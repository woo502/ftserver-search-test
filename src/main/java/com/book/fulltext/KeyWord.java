//Free
package com.book.fulltext;

import iBoxDB.LocalServer.DatabaseConfig;
import iBoxDB.LocalServer.NotColumn;

public abstract class KeyWord {

    public final static int MAX_WORD_LENGTH = 16;

    public static void config(DatabaseConfig c) {

        // English Language or Word (max=16)              
        c.EnsureTable(KeyWordE.class, "/E", "K(" + MAX_WORD_LENGTH + ")", "I", "P", "T", "O");

        // Non-English Language or Character
        c.EnsureTable(KeyWordN.class, "/N", "K", "I", "P", "T", "O");

    }

    @NotColumn
    public abstract Object getKeyWord();

    @NotColumn
    public abstract void setKeyWord(Object k);

    @NotColumn
    public abstract int size();

    //Position
    public int P;

    @NotColumn
    public int getPosition() {
        return P;
    }

    @NotColumn
    public void setPosition(int p) {
        P = p;
    }

    //Document ID
    public long I;

    @NotColumn
    public long getID() {
        return I;
    }

    @NotColumn
    public void setID(long i) {
        I = i;
    }
    
    //Category Type    
    public byte T;

    @NotColumn
    public byte getType() {
		return T;
	}

    @NotColumn
	public void setType(byte t) {
		T = t;
	}
    
	//Category Type    
    public int O;

    @NotColumn
	public int getO() {
		return O;
	}

    @NotColumn
	public void setO(int o) {
		O = o;
	}

	@NotColumn
    public KeyWord previous;
    @NotColumn
    public boolean isLinked;
    @NotColumn
    public boolean isLinkedEnd;

    @NotColumn
    public String toFullString() {
        return (previous != null ? previous.toFullString() + " -> " : "") + toString();
    }
}
