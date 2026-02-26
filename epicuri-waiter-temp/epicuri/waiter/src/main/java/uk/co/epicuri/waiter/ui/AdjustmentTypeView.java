package uk.co.epicuri.waiter.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;

/**
 * Created by antonandreev on 13.03.18.
 */

public class AdjustmentTypeView extends android.support.v7.widget.AppCompatButton {
    private EpicuriAdjustmentType type;

    public AdjustmentTypeView(Context context) {
        super(context);
    }

    public AdjustmentTypeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AdjustmentTypeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AdjustmentTypeView(@NonNull Context context, EpicuriAdjustmentType type) {
        super(context);
        init(type);
    }

    private void init(EpicuriAdjustmentType adjustment) {
        this.type = adjustment;
        setBackgroundResource(R.color.green);
        setText(adjustment.getShortCode() != null ? adjustment.getShortCode() : adjustment.getName());
        int colorId = adjustment.getType() == EpicuriAdjustmentType.TYPE_PAYMENT ? R.color
                .green : R.color.red;
        getBackground().setColorFilter(ContextCompat.getColor(getContext(), colorId),
                PorterDuff.Mode.MULTIPLY);
        setId(adjustment.hashCode());
        setTag(type);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
}
