package com.book.fts;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.book.bean.BPage;
import com.book.bean.Pair;
import com.book.common.SDB;
import com.book.fulltext.Engine;

public class SearchResource {

    public static ConcurrentLinkedDeque<String> searchList
            = new ConcurrentLinkedDeque<String>();

    public static ConcurrentLinkedDeque<String> urlList
            = new ConcurrentLinkedDeque<String>();

    public static ConcurrentLinkedDeque<Pair> pairList
            = new ConcurrentLinkedDeque<Pair>();

    public static ConcurrentLinkedDeque<String> waitingUrlList
            = new ConcurrentLinkedDeque<String>();

    public static ConcurrentLinkedDeque<Pair> waitingPairList
            = new ConcurrentLinkedDeque<Pair>();

    private final static int batchCommit = 200;
    public final static Engine engine = new Engine();

    public static String indexText(String url, boolean isDelete, HashSet<String> subUrls, Byte type) {

        for (BPage p : SDB.search_db.select(BPage.class, "from Page where url==?", url)) {
            engine.indexTextNoTran(SDB.search_db, batchCommit, p.id, p.content.toString(), type, 0, true);
            engine.indexTextNoTran(SDB.search_db, batchCommit, p.rankUpId(), p.rankUpDescription(), type, 0, true);
            SDB.search_db.delete("Page", p.id);
        }

        if (isDelete) {
            return "deleted";
        }

        BPage p = BPage.get(url, subUrls);
        if (p == null) {
            return "temporarily unreachable";
        } else {
            p.id = SDB.search_db.newId();
            SDB.search_db.replace("Page", p);
            engine.indexTextNoTran(SDB.search_db, batchCommit, p.id, p.content.toString(), type, 0, false);
            engine.indexTextNoTran(SDB.search_db, batchCommit, p.rankUpId(), p.rankUpDescription(), type, 0, false);
            return p.url;
        }

    }

    public static String indexText(Pair target, boolean isDelete) {

    	String url = (String)target.getKey();
    	Byte type = (Byte)target.getValue();
    	
        for (BPage p : SDB.search_db.select(BPage.class, "from Page where url==?", url)) {
            engine.indexTextNoTran(SDB.search_db, batchCommit, p.rankUpId(), p.allContent(), type, p.priority, true);
//            engine.indexTextNoTran(SDB.search_db, batchCommit, p.rankUpId(), p.rankUpDescription(), type, true);
            SDB.search_db.delete("Page", p.id);
        }

        if (isDelete) {
            return "deleted";
        }

        BPage p = BPage.getJSON(url);
        if (p == null) {
            return "temporarily unreachable";
        } else {
        	p.type = type;
            p.id = SDB.search_db.newId(1);
            boolean ret1 = SDB.search_db.replace("Page", p);
            engine.indexTextNoTran(SDB.search_db, batchCommit, p.rankUpId(), p.allContent(), type, p.priority, false);
//            engine.indexTextNoTran(SDB.search_db, batchCommit, p.rankUpId(), p.rankUpDescription(), type, false);
            return p.url;
        }

    }

}
