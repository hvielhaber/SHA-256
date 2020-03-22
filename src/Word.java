import java.io.PrintStream;

public class Word {

    static final long bitMask = 0xFFFFFFFFL;     // mask 32 bits

    private long val;

    public Word(long val) {
        this.val = val & bitMask;
    }

    public Word and(Word w)        { return new Word((val & w.val) & bitMask);                    }
    public Word or(Word w)         { return new Word((val | w.val) & bitMask);                    }
    public Word xor(Word w)        { return new Word((val ^ w.val) & bitMask);                    }
    public Word compl()            { return new Word((~val) & bitMask);               }
    public Word rightShift(int n)  { return new Word((val >> n) & bitMask);                       }
    public Word leftShift(int n)   { return new Word((val << n) & bitMask);           }
    //public Word plus(Word w)       { return new Word((val + w.val) & bitMask);        }
    public Word plus(Word w)       {
        if (w == null) {
            System.out.println("plus- w null");
        }
        return new Word((val + w.val) & bitMask);
    }
    public Word leftRotate(int n)  { return new Word(((val << n) | (val >> (32 - n))) & bitMask); }
    public Word rightRotate(int n) { return new Word(((val >> n) | (val << (32 - n))) & bitMask); }

    public void print(PrintStream out) {
        out.print(String.format("%08x", val));
    }
}
