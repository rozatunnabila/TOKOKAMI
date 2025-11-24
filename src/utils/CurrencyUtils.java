package utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class CurrencyUtils {
    private static final DecimalFormatSymbols ID_SYMBOLS = new DecimalFormatSymbols(new Locale("id", "ID"));
    
    static {
        ID_SYMBOLS.setGroupingSeparator('.');
        ID_SYMBOLS.setDecimalSeparator(',');
    }

    private CurrencyUtils() {}

    public static String format(double amount) {
        // Konversi nilai ke Rupiah (misal 300 -> 300.000)
        double convertedAmount = amount * 1000;
        
        DecimalFormat formatter = new DecimalFormat("#,##0", ID_SYMBOLS);
        formatter.setGroupingUsed(true);
        formatter.setMaximumFractionDigits(0);
        
        return "Rp " + formatter.format(convertedAmount);
    }
}


