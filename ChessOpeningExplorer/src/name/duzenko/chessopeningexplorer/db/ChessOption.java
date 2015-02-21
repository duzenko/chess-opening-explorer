package name.duzenko.chessopeningexplorer.db;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class ChessOption {

	/*static public class ChessMove {
		public ChessMove(int idx, int offset) {
		}
		public int stat[] = {0, 0, 0};
		int offset;
	}*/

	
	public int TxtPos, TxtOffset;

	public int First;

	public int Next;

	public int stat[] = {0, 0, 0};
	
	public int fileRecNo;
	public String move = "";
	
	static final int recordSize = 24;
	static byte recData[] = new byte[recordSize]; 
	static IntBuffer buffer = ByteBuffer.wrap(recData).order(ByteOrder.nativeOrder()).asIntBuffer();
	
	public ChessOption(int idx, int offset) {
		fileRecNo = idx;
		TxtOffset = offset;
	}

	public void load(RandomAccessFile stream) throws IOException {
		stream.seek(fileRecNo*ChessOption.recordSize);
		stream.read(recData);
		buffer.rewind();
		TxtPos = buffer.get();
		First = buffer.get();
		Next = buffer.get();
		stat[0] = buffer.get();
		stat[1] = buffer.get();
		stat[2] = buffer.get();
	}

}
