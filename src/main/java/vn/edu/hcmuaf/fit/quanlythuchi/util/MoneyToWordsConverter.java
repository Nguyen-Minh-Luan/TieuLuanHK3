package vn.edu.hcmuaf.fit.quanlythuchi.util;

public class MoneyToWordsConverter {
    private static final String[] digits = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};

    public static String convert(long amount) {
        if (amount == 0) {
            return "Không đồng chẵn";
        }
        if (amount < 0) {
            return "Âm " + convert(Math.abs(amount));
        }

        String res = "";
        long code = amount;

        long billion = code / 1000000000L;
        code %= 1000000000L;

        long million = code / 1000000L;
        code %= 1000000L;

        long thousand = code / 1000L;
        long unit = code % 1000L;

        if (billion > 0) {
            res += readGroup3(billion, true) + " tỷ ";
        }
        if (million > 0) {
            res += readGroup3(million, billion > 0) + " triệu ";
        }
        if (thousand > 0) {
            res += readGroup3(thousand, billion > 0 || million > 0) + " nghìn ";
        }
        if (unit > 0) {
            res += readGroup3(unit, billion > 0 || million > 0 || thousand > 0);
        }

        res = res.trim();

        if (!res.isEmpty()) {
            res = res.substring(0, 1).toUpperCase() + res.substring(1) + " đồng chẵn";
        }
        return res;
    }

    private static String readGroup3(long n, boolean showZeroHundreds) {
        long hundred = n / 100;
        long ten = (n % 100) / 10;
        long unit = n % 10;

        StringBuilder sb = new StringBuilder();

        if (hundred > 0 || showZeroHundreds) {
            sb.append(digits[(int) hundred]).append(" trăm ");
        }

        if (ten > 0) {
            if (ten == 1) {
                sb.append("mười ");
            } else {
                sb.append(digits[(int) ten]).append(" mươi ");
            }
        } else if (hundred > 0 || showZeroHundreds) {
            if (unit > 0) {
                sb.append("lẻ ");
            }
        }

        if (unit > 0) {
            if (unit == 1 && ten > 1) {
                sb.append("mốt");
            } else if (unit == 5 && ten > 0) {
                sb.append("lăm");
            } else if (unit == 4 && ten > 1) {
                sb.append("tư");
            } else {
                sb.append(digits[(int) unit]);
            }
        }

        return sb.toString().trim();
    }
}
