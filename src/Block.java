import java.io.PrintStream;

public class Block {

    private Word[] words;

    public Block(byte[] bytes, int start, int len) {
        words = new Word[16];
        for (int i = start; i < start+len; i += 4) {
            long val = 0;
            for (int j = 0; j < 4; j++) {
                val = val << 8 | Byte.toUnsignedLong(bytes[i + j]);
            }
            words[(i-start)/4] = new Word(val);
        }
    }

    public Word get(int j) {
        return words[j];
    }

    public void print(PrintStream out) {
        for (Word word: words) {
            word.print(out);
        }
        out.println();
    }




}
