package name.duzenko.chessopeningexplorer;

import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class StatView extends View {
	
	int w, stat[] = {0, 0, 0};
	private Paint paint = new Paint();
    RectF r = new RectF();

    public StatView(Context context) {
        super(context);
    }

    public StatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Style.FILL);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(16 * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        r.bottom = h;
      }

    int colors[] = {0xFF008000, 0xFF808080, 0xFF800000};
    
	@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int total = stat[0] + stat[1] + stat[2];
        r.right = 0;
        for (int i = 0; i < 3; i++) {
            if (stat[i] == 0)
            	continue;
            float sw = w*stat[i]/total;
            r.left = r.right;
        	r.right = r.left + sw;
        	paint.setColor(colors[i]);
        	canvas.drawRect(r, paint);
        	String text = String.format(Locale.getDefault(), "%d%%", 100*stat[i]/total); 
        	float tw = paint.measureText(text);
        	if (tw > sw)
        		continue;
        	paint.setColor(Color.WHITE);
        	canvas.drawText(text, (r.left + r.right)/2, r.height()-12, paint);
        }
    }

}
