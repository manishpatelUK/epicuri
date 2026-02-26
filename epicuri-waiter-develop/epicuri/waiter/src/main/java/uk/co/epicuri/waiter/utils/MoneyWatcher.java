package uk.co.epicuri.waiter.utils;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;

import uk.co.epicuri.waiter.interfaces.NumberFormatCallback;

/**
 * Created by manish on 04/03/2018.
 */

public class MoneyWatcher implements TextWatcher {
    private final NumberFormatCallback callback;
    private final DecimalFormat decimalFormatter;
    private final EditText et;

    public MoneyWatcher(EditText editText, String pattern, NumberFormatCallback callback) {
        this.callback = callback;
        this.decimalFormatter = new DecimalFormat(pattern);
        this.et = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        et.removeTextChangedListener(this);
        if (s != null && !s.toString().isEmpty()) {
            String current = s.toString();

            if(current.length() > 0 && !current.endsWith(".") && current.contains(".")) {
                String[] bits = current.split("\\.");
                if(bits.length == 2 && bits[1].length() > 2) {
                    try {
                        String formatted = decimalFormatter.format(Double.valueOf(bits[0] + "." + bits[1].substring(0,2)));
                        et.setText(formatted);
                        et.setSelection(formatted.length());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        if(callback != null) {
            callback.finishedFormatting();
        }

        et.addTextChangedListener(this);
    }
}
