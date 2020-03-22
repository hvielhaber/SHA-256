import java.io.PrintStream;

public class SHA256 implements Runnable {

    public final byte paddingByte1 = (byte)0x80;
    public final byte paddingByteN = (byte)0x00;

    static private final Word[] H_init = new Word[9];

    static {
        H_init[1] = new Word(0x6a09e667L);
        H_init[2] = new Word(0xbb67ae85L);
        H_init[3] = new Word(0x3c6ef372L);
        H_init[4] = new Word(0xa54ff53aL);
        H_init[5] = new Word(0x510e527fL);
        H_init[6] = new Word(0x9b05688cL);
        H_init[7] = new Word(0x1f83d9abL);
        H_init[8] = new Word(0x5be0cd19L);
    }

    private byte[] input;
    private Block[] M;
    private int N;
    private Word[] H;
    private Word a, b, c, d, e, f, g, h;
    Word[] W = new Word[64];

    static Word[] K = new Word[] {
            new Word(0x428a2f98L),
            new Word(0x71374491L),
            new Word(0xb5c0fbcfL),
            new Word(0xe9b5dba5L),
            new Word(0x3956c25bL),
            new Word(0x59f111f1L),
            new Word(0x923f82a4L),
            new Word(0xab1c5ed5L),
            new Word(0xd807aa98L),
            new Word(0x12835b01L),
            new Word(0x243185beL),
            new Word(0x550c7dc3L),
            new Word(0x72be5d74L),
            new Word(0x80deb1feL),
            new Word(0x9bdc06a7L),
            new Word(0xc19bf174L),
            new Word(0xe49b69c1L),
            new Word(0xefbe4786L),
            new Word(0x0fc19dc6L),
            new Word(0x240ca1ccL),
            new Word(0x2de92c6fL),
            new Word(0x4a7484aaL),
            new Word(0x5cb0a9dcL),
            new Word(0x76f988daL),
            new Word(0x983e5152L),
            new Word(0xa831c66dL),
            new Word(0xb00327c8L),
            new Word(0xbf597fc7L),
            new Word(0xc6e00bf3L),
            new Word(0xd5a79147L),
            new Word(0x06ca6351L),
            new Word(0x14292967L),
            new Word(0x27b70a85L),
            new Word(0x2e1b2138L),
            new Word(0x4d2c6dfcL),
            new Word(0x53380d13L),
            new Word(0x650a7354L),
            new Word(0x766a0abbL),
            new Word(0x81c2c92eL),
            new Word(0x92722c85L),
            new Word(0xa2bfe8a1L),
            new Word(0xa81a664bL),
            new Word(0xc24b8b70L),
            new Word(0xc76c51a3L),
            new Word(0xd192e819L),
            new Word(0xd6990624L),
            new Word(0xf40e3585L),
            new Word(0x106aa070L),
            new Word(0x19a4c116L),
            new Word(0x1e376c08L),
            new Word(0x2748774cL),
            new Word(0x34b0bcb5L),
            new Word(0x391c0cb3L),
            new Word(0x4ed8aa4aL),
            new Word(0x5b9cca4fL),
            new Word(0x682e6ff3L),
            new Word(0x748f82eeL),
            new Word(0x78a5636fL),
            new Word(0x84c87814L),
            new Word(0x8cc70208L),
            new Word(0x90befffaL),
            new Word(0xa4506cebL),
            new Word(0xbef9a3f7L),
            new Word(0xc67178f2L)
        };

    public static void main(String[] args) {
        byte[] bytes = args[0].getBytes();
        SHA256 sha256 = new SHA256(bytes);
        sha256.run();
    }

    public SHA256(byte[] input) {
        this.input = input;
    }

    @Override
    public void run() {
        byte[] padded = pad(input);
        M = split(padded);
        // System.out.print("M[1]: "); M[1].print(System.out);
        N = M.length-1;

        init_H();
        // System.out.print("[0] "); printHash(System.out);

        for (int i = 1; i <= N; i++) {
            initRegisters(H);
            sha256Compression(i);
            intermedateHash();
            // System.out.print("["+ i +"] "); printHash(System.out);
        }

        printHash(System.out);
    }

    private byte[] pad(byte[] input) {
        int l = input.length * 8;
        int k = 448 - (l + 1);
        if (k < 0) {
            k += 512;
        }
        int lPadded = l + 1 + k + 64;

        byte[] padded = new byte[lPadded / 8];
        System.arraycopy(input, 0, padded, 0, input.length);

        int j = input.length;
        padded[j++] = paddingByte1;
        for (int i = 0; i < ((k+1) / 8) - 1; i++) {
            padded[j++] = paddingByteN;
        }

        int msgLen = l;
        for (int i = 7; i >= 0; i--, msgLen /= 256) {
            padded[j+i] = (byte)(msgLen % 256);
        }

        return padded;
    }

    private Block[] split(byte[] bytes) {
        int nrBlocks = (bytes.length / 64) + (bytes.length % 64 > 0 ? 1 : 0) + 1;
        Block[] blocks = new Block[nrBlocks];
        for (int i = 1; i < nrBlocks; i++) {
            blocks[i]  = new Block(bytes, (i-1) * 64, 64);
        }
        return blocks;
    }

    private void init_H() {
        H = new Word[9];
        for (int i = 1; i <= 8; i++) {
            H[i] = H_init[i];
        }
    }

    private void initRegisters(Word[] H) {
        a = H[1];
        b = H[2];
        c = H[3];
        d = H[4];
        e = H[5];
        f = H[6];
        g = H[7];
        h = H[8];
    }

    private void sha256Compression(int i) {
        for (int j = 0; j < 64; j++) {
            if (j < 16) {
                W[j] = M[i].get(j);
            } else {
                W[j] = sigma1(W[j-2]).plus(W[j-7]).plus(sigma0(W[j-15])).plus(W[j-16]);
            }
            Word T1 = h.plus(Sigma1(e)).plus(Ch(e, f, g)).plus(K[j]).plus(W[j]);
            Word T2 = Sigma0(a).plus(Maj(a, b, c));
            h = g;
            g = f;
            f = e;
            e = d.plus(T1);
            d = c;
            c = b;
            b = a;
            a = T1.plus(T2);
            // System.out.print("R["+j+"] "); printRegisters(System.out);
        }
    }

    private Word Ch(Word x, Word y, Word z)   { return x.and(y).xor(x.compl().and(z));        }
    private Word Maj(Word x, Word y, Word z)  { return x.and(y).xor(x.and(z)).xor(y.and(z)); }
    private Word Sigma0(Word x) { return x.rightRotate(2).xor(x.rightRotate(13)).xor(x.rightRotate(22)); }
    private Word Sigma1(Word x) { return x.rightRotate(6).xor(x.rightRotate(11)).xor(x.rightRotate(25)); }
    private Word sigma0(Word x) { return x.rightRotate(7).xor(x.rightRotate(18)).xor(x.rightShift(3));   }
    private Word sigma1(Word x) { return x.rightRotate(17).xor(x.rightRotate(19)).xor(x.rightShift(10)); }

    private void intermedateHash() {
        H[1] = a.plus(H[1]);
        H[2] = b.plus(H[2]);
        H[3] = c.plus(H[3]);
        H[4] = d.plus(H[4]);
        H[5] = e.plus(H[5]);
        H[6] = f.plus(H[6]);
        H[7] = g.plus(H[7]);
        H[8] = h.plus(H[8]);
    }

    private void printHash(PrintStream out) {
        for (int i = 1; i <= 8; i++) {
            H[i].print(out);
        }
        out.println();
    }

    private void printRegisters(PrintStream out) {
        a.print(out);
        b.print(out);
        c.print(out);
        d.print(out);
        e.print(out);
        f.print(out);
        g.print(out);
        h.print(out);
        out.println();
    }
}
