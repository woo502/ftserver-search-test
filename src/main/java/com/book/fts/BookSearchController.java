package com.book.fts;

import iBoxDB.LocalServer.Box;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.book.bean.BPage;
import com.book.bean.HotWord;
import com.book.bean.Pair;
import com.book.bean.RetJson;
import com.book.common.SDB;
import com.book.fulltext.KeyWord;
import com.book.util.MD5Util;
import com.book.util.SensitiveWord;
import com.book.util.StringUtil;

@Controller
@RequestMapping(value = "/book")
public class BookSearchController {

	final static Logger logger = LoggerFactory.getLogger(BookSearchController.class);
	
	private final static int SleepTime = 2000;
	public static Throwable lastEx;
	private final ExecutorService writeES = Executors.newSingleThreadExecutor();
	private final static ExecutorService updateHW = Executors
			.newSingleThreadExecutor();

	@Autowired
	private SensitiveWord wordFilter;
	
	@RequestMapping(value = "/search")
	@ResponseBody
	public RetJson search(@RequestParam(defaultValue="")String name, 
			@RequestParam(name="type[]", required=false)Byte[] type, Long startId,
			@RequestParam(defaultValue = "12") Integer pageCount) {

		RetJson retJson = new RetJson(0, null);
		
		if (StringUtil.isEmpty(name)) {
			return new RetJson(-2, null);
		}
		
		if (startId == null) {
			startId = Long.MAX_VALUE;
		}

		Box box = null;
		try {
			box = SDB.search_db.cube();
			JSONArray jsonArray = new JSONArray();
			final List<KeyWord> illegalKw = new ArrayList<KeyWord>();
			// 搜索关键字
			for (KeyWord kw : SearchResource.engine.searchDistinct(box, name,
					type, startId, pageCount)) {
				startId = kw.getID() - 1;
				long id = kw.getID();
				id = BPage.rankDownId(id, kw.getO());
				BPage p = box.d("Page", id).select(BPage.class);
				if (p != null) {
					p.keyWord = kw;
					JSONObject json = JSON.parseObject(p.content.toString());
					json.put("searchId", kw.getID()+"");
					jsonArray.add(json);
				} else {
					logger.error("bpage not found, keyword id: " + id + ", keyword is " + kw.getKeyWord());
					illegalKw.add(kw);
				}
			}
			
			if (illegalKw.size() > 0) {
				updateHW.submit(new Runnable() {
					@Override
					public void run() {

						Box box2 = null;
						try {
							box2 = SDB.search_db.cube();
							if (box2 != null) {
								for (KeyWord kw: illegalKw) {
									SearchResource.engine.insertToBox(box2, kw, true);
								}
							}
						} catch (Exception e) {
							logger.error("keyword delete error", e);
						} finally {
							if (box2 != null) {
								box2.close();
							}
						}
					}
				});
			}
			
			// 保存热门关键字
			if (jsonArray.size() > 0) {
				
				// 过滤敏感词
				if (!wordFilter.ContainSensitive(name)) {
					String sql = "from hotW where kw == ?";
					if (type != null) {
						sql += " & T == ?";
					}
					sql += "limit 1";
					Iterator<HotWord> hotWords = box.select(HotWord.class, sql, name, type == null ? null : type[0]).iterator();
					if (!hotWords.hasNext()) {
						long newId = SDB.search_db.newId(2);
						boolean ret = SDB.search_db.insert("hotW",new HotWord(newId, name, type == null ? null : type[0]));
					} else {
						final HotWord hw = hotWords.next();
						updateHW.submit(new Runnable() {
							@Override
							public void run() {
								hw.count++;
								SDB.search_db.update("hotW", hw);
							}
						});
					}
				}
			}
			
			JSONObject data = new JSONObject();
			data.put("list", jsonArray);
			retJson.setData(data);
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			retJson = new RetJson(-1, null);
		} finally {
			if (box != null) {
				box.close();
			}
		}
		
		return retJson;
	}

	@RequestMapping(value = "/hotword")
	@ResponseBody
	public RetJson hotWord(@RequestParam(required=false)String filter,
						@RequestParam(required=false, defaultValue="0")Byte type) {

		RetJson retJson = new RetJson(0, null);
		
		Box box = null;
		try {
			box = SDB.search_db.cube();
			JSONArray jsonArray = new JSONArray();
			
			Object arg1 = null;
			Object arg2 = null;
			Object arg3 = null;
			
			String sql = "from hotW";
			if (StringUtil.isNotEmpty(filter)) {
				sql += " where kw >= ? & kw < ?";
				arg1 = filter;
				char charAt = filter.charAt(filter.length()-1);;
				long bigger = charAt + 1 > 65533 ? 65533 : charAt + 1;
				arg2 = filter.replace(charAt, (char)bigger);
			}
			if (type != null && type > 0) {
				if (arg1 != null) {
					sql += " & T == ?";
					arg3 = type;
				} else {
					arg1 = type;
					sql += " where T == ?";
				}
			}
			
			sql += " order by count desc limit 0, 12";
			
			Iterable<HotWord> hotWords = box.select(HotWord.class, sql, arg1, arg2, arg3);
			for (HotWord hw: hotWords) {
				jsonArray.add(hw.getKw());
			}
			
			JSONObject data = new JSONObject();
			data.put("list", jsonArray);
			retJson.setData(data);
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			retJson = new RetJson(-1, null);
		} finally {
			if (box != null) {
				box.close();
			}
		}
		
		return retJson;
	}
	
	@RequestMapping(value = "/clearHotWord")
	@ResponseBody
	public RetJson clearHotWord(@RequestParam(required=false)String filter,
						@RequestParam(required=false, defaultValue="1")Byte type) {

		RetJson retJson = new RetJson(0, null);
		
		Box box = null;
		try {
			box = SDB.search_db.cube();
			Iterable<HotWord> hotWords = box.select(HotWord.class, "from hotW where T=?", type);
			for (HotWord hw: hotWords) {
				SDB.search_db.delete("hotW", hw.id);
			}
			
			JSONObject data = new JSONObject();
			retJson.setData(data);
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			retJson = new RetJson(-1, null);
		} finally {
			if (box != null) {
				box.close();
			}
		}
		
		return retJson;
	}

	@RequestMapping(value = "/index")
	@ResponseBody
	public RetJson index(@RequestBody String param) {

		try {
			logger.info("index request: "+ param);
			
			JSONObject json = JSON.parseObject(param);
			
			if (json == null) {
				return new RetJson(-2, null);
			}
			
			JSONArray list = json.getJSONArray("list");
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				JSONObject obj = (JSONObject)iterator.next();
				Long id = obj.getLong("id");
				Byte type = obj.getByte("type");
				Byte isDelete = obj.getByte("isDelete") == null ? 1 : obj.getByte("isDelete");
				final boolean del = isDelete == 2;
				
				if (id == null || id == 0 || type == null || type == 0) {
					continue;
				}
				
				// 从其他服务拉取内容列表
				final String url = "http://xxx.com/cgi/index.php?p=admin&c=search&m=addData&id={1}&type={2}"
						.replace("{1}", id.toString()).replace("{2}",
								type.toString())+"&secret="+MD5Util.md5(id.toString()+type.toString()+"PYcnTdNj6dfjvKmp");
				final Byte T = type;
				writeES.submit(new Runnable() {
					@Override
					public void run() {
						try {
							Pair pair = new Pair(url, T);
							SearchResource.indexText(pair, del);
						} catch (Throwable ex) {
							logger.error(ex.getMessage(), ex);
						}
					}
				});
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RetJson(-1, null);
		}

		return new RetJson(0, null);
	}
}
