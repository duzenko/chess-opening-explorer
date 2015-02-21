package name.duzenko.chessopeningexplorer;

import name.duzenko.chessopeningexplorer.db.ChessOption;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MoveAdapter extends ArrayAdapter<ChessOption> {
	
	public MoveAdapter(Context context, int res) {
		super(context, res);
	}
	
	void sort() {
		for(int i=0; i<getCount()-1; i++) 
			for(int j=getCount()-1; j>i; j--) {
				ChessOption m1 = getItem(j-1), m2 = getItem(j);
				if(m1.stat[0]+m1.stat[1]+m1.stat[2]<m2.stat[0]+m2.stat[1]+m2.stat[2]) {
					remove(m2);
					insert(m2, j-1);
				}
			}
				
	}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	if(convertView==null) {
    		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		convertView = inflater.inflate(R.layout.listitem_move, parent, false);
    	} 
    		
    	ChessOption chessMove = getItem(position); 
        TextView textView = (TextView) convertView.findViewById(R.id.textMove);
        textView.setText(chessMove.move);
        textView = (TextView) convertView.findViewById(R.id.textStat);
        textView.setText(String.valueOf(chessMove.stat[0]) + "w " + String.valueOf(chessMove.stat[2]) + "d " + String.valueOf(chessMove.stat[1]) + "b");

        return convertView;
    }
    
}
