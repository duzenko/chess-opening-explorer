package name.duzenko.chessopeningexplorer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class ChessOption {

	static final int recordSize = 24;
	
	int SrcPos, First, Next, Results[] = {0, 0, 0};
	
	//static ByteBuffer src;
	
	byte recData[] = new byte[recordSize]; 
	IntBuffer buffer = ByteBuffer.wrap(recData).order(ByteOrder.nativeOrder()).asIntBuffer();
	void load(RandomAccessFile stream) throws IOException {
		stream.read(recData);
		buffer.rewind();
		SrcPos = buffer.get();
		First = buffer.get();
		Next = buffer.get();
		Results[0] = buffer.get();
		Results[1] = buffer.get();
		Results[2] = buffer.get();
	}

}
