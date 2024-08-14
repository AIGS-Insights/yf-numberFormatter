package com.aigsinsights.formatters;

import com.hof.mi.interfaces.CustomFormatter;
import com.hof.util.UtilString;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays; 
import java.util.Currency;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberFormatter extends CustomFormatter {
    
    public String getName() {
        return "Number Formatter";
    }
    
    public boolean acceptsNativeType(int type) {
        // We only handle numeric types
        if (type == TYPE_NUMERIC) return true;
        return false;
    }

    public boolean returnsHtml() {
        return true;
    }
    
    public boolean hasDependentParameters(String paramString) {
        return ("FORMAT".equals(paramString));
    }
    
    public boolean isParameterRequired(String paramString) {
        if ("CUSTOM".equals(paramString))
            return (isParameterRequired("FORMAT") && "CUSTOM".equals(getParameterValue("FORMAT"))); 
        return true;
    }
    
    public void setupParameters() {
        String currency = Currency.getInstance(Locale.getDefault()).getSymbol();
        // Format String parameter
        Parameter formatParam = new Parameter();
        formatParam.setUniqueKey("FORMAT");
        formatParam.setDisplayName("Format String");
        formatParam.setDescription("Select the format mask to be applied to numeric values");
        formatParam.setDataType(TYPE_TEXT);
        formatParam.setDisplayType(DISPLAY_SELECT);
        formatParam.addOption("#,##0;-#,##0", "#,##0;-#,##0");
        formatParam.addOption("#,##0;[Red]-#,##0", "#,##0;[Red]-#,##0");
        formatParam.addOption("#,##0.00;-#,##0.00", "#,##0.00;-#,##0.00");
        formatParam.addOption("#,##0.00;[Red]-#,##0.00", "#,##0.00;[Red]-#,##0.00");
        formatParam.addOption(currency+"#,##0;-"+currency+"#,##0", currency+"#,##0;-"+currency+"#,##0");
        formatParam.addOption(currency+"#,##0;[Red]-"+currency+"#,##0", currency+"#,##0;[Red]-"+currency+"#,##0");
        formatParam.addOption(currency+"#,##0.00;-"+currency+"#,##0.00", currency+"#,##0.00;-"+currency+"#,##0.00");
        formatParam.addOption(currency+"#,##0.00;[Red]-"+currency+"#,##0.00", currency+"#,##0.00;[Red]-"+currency+"#,##0.00");
        formatParam.addOption("CUSTOM", "Other");
        formatParam.setDefaultValue("#,##0;-#,##0");
        addParameter(formatParam);
        
        // Custom Format String parameter
        Parameter customParam = new Parameter();
        customParam.setUniqueKey("CUSTOM");
        customParam.setDisplayName("Custom Format String");
        customParam.setDescription("Define a custom format mask to be applied to numeric values");
        customParam.setDataType(TYPE_TEXT);
        customParam.setDisplayType(DISPLAY_TEXT_LONG);
        //customParam.setDefaultValue("#,##0.00;-#,##0.00");
        addParameter(customParam);
        
        // Seperator parameter
        Parameter seperatorParam = new Parameter();
        seperatorParam.setUniqueKey("SEPERATOR");
        seperatorParam.setDisplayName("Seperator");
        seperatorParam.setDescription("Character to be use as a seperator");
        seperatorParam.setDataType(TYPE_TEXT);
        seperatorParam.setDisplayType(DISPLAY_TEXT_TINY);
        seperatorParam.setDefaultValue(",");
        addParameter(seperatorParam);
        
        // Decimal parameter
        Parameter decimalParam = new Parameter();
        decimalParam.setUniqueKey("DECIMAL");
        decimalParam.setDisplayName("Decimal");
        decimalParam.setDescription("Character to be use as a decimal.");
        decimalParam.setDataType(TYPE_TEXT);
        decimalParam.setDisplayType(DISPLAY_TEXT_TINY);
        decimalParam.setDefaultValue(".");
        addParameter(decimalParam);
        
        // NULL To Zero parameter
        Parameter nullParam = new Parameter();
        nullParam.setUniqueKey("NULLTOZERO");
        nullParam.setDisplayName(UtilString.getResourceString("mi.text.column.format.nullzero"));
        nullParam.setDataType(TYPE_BOOLEAN);
        nullParam.setDisplayType(DISPLAY_RADIO);
        nullParam.addOption(Boolean.TRUE, UtilString.getResourceString("mi.text.yes"), UtilString.getResourceString("mi.text.column.format.nullzero.on"));
        nullParam.addOption(Boolean.FALSE, UtilString.getResourceString("mi.text.no"), UtilString.getResourceString("mi.text.column.format.nullzero.off"));
        nullParam.setDefaultValue(Boolean.FALSE);
        addParameter(nullParam);

        // Rounding parameter
        Parameter roundingParam = new Parameter();
        roundingParam.setUniqueKey("ROUNDING");
        roundingParam.setDisplayName("Rounding");
        roundingParam.setDescription(UtilString.getResourceString("mi.text.column.format.rounding.desc"));
        roundingParam.setDataType(TYPE_TEXT);
        roundingParam.setDisplayType(DISPLAY_SELECT);
        //roundingParam.addOption("NONE","None");
        roundingParam.addOption("UP",UtilString.getResourceString("mi.text.round.up"));
        roundingParam.addOption("DOWN", UtilString.getResourceString("mi.text.round.down"));
        roundingParam.addOption("CEILING", "Ceiling");
        roundingParam.addOption("FLOOR", "Floor");
        roundingParam.addOption("HALF_UP", UtilString.getResourceString("mi.text.round.half.up"));
        roundingParam.addOption("HALF_DOWN", UtilString.getResourceString("mi.text.round.half.down"));
        roundingParam.addOption("HALF_EVEN", "Half Even");
        roundingParam.setDefaultValue("HALF_UP");
        addParameter(roundingParam);
    }
    
    public String render(Object value, int renderType) throws Exception {
        try {
            DecimalFormat pattern = new DecimalFormat();
            String format = (String) getParameterValue("FORMAT");
            format = ("CUSTOM".equals(format)) ? (String) getParameterValue("CUSTOM") : format;
            String separator = (String) getParameterValue("SEPERATOR");
            String decimal = (String) getParameterValue("DECIMAL");
            Boolean nulltozero = (Boolean) getParameterValue("NULLTOZERO");
            String rounding = (String) getParameterValue("ROUNDING");
            Double dvalue = null;
            if (value == null && nulltozero) {
                dvalue = 0.0;
            } else if (value == null) {
                return null;
            } else {
                dvalue = Double.valueOf(value.toString());
            }
            System.out.println("value = " + dvalue);
            System.out.println("format = " + format);
            String[] patterns = format.split(";");
            if (patterns.length == 1) patterns = new String[] {format, "-"+format};
            System.out.println("length = " + patterns.length);
            System.out.println("separator = " + separator);
            System.out.println("decimal = " + decimal);
            System.out.println("rounding = " + rounding);
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            if (separator != null && separator.length() > 0) {
                System.out.println("setting separator");
                symbols.setGroupingSeparator(separator.charAt(0));
            }
            if (decimal != null && decimal.length() > 0) {
                System.out.println("setting decimal");
                symbols.setDecimalSeparator(decimal.charAt(0));
            }
            pattern.setDecimalFormatSymbols(symbols);
            pattern.setRoundingMode(RoundingMode.valueOf(rounding));
            
            if (dvalue == 0 && patterns.length > 2) {
                System.out.println("value is 0 and formatted");
                Pattern p = Pattern.compile("\\[(.*?)\\]");
                Matcher m = p.matcher(patterns[2]);
                System.out.println("patterns[2] = " + patterns[2]);
                while (m.find()) {
                    System.out.println("in.find() = " + m.group());
                    System.out.println("matcher.find() = " + m.group(0));
                    System.out.println("matcher.find() = " + m.group(1));
                    return "<font style=\"color: " + m.group(1) + ";\">" + patterns[2].replace(m.group(0), "") + "</font>";
                }
                return patterns[2];
            }
            format = String.join(";", Arrays.copyOfRange(patterns, 0, 2));
            System.out.println("new format = " + format);
            
            if (dvalue < 0 && patterns.length > 1) {
                System.out.println("value is less than 0 and formatted");
                //Pattern p = Pattern.compile("\\[(.*?)\\]");
                //Matcher m = p.matcher(patterns[1]);
                System.out.println("patterns[1] = " + patterns[1]);
            
                pattern.applyPattern(format);
                return pattern.format(dvalue);
            }
            if (patterns.length >= 1) {
                System.out.println("value is 0 or greater and formatted");
                Pattern p = Pattern.compile("\\[(.*?)\\]");
                Matcher m = p.matcher(patterns[0]);
                System.out.println("patterns[0] = " + patterns[0]);
                while (m.find()) {
                    System.out.println("in.find() = " + m.group());
                    System.out.println("matcher.find() = " + m.group(0));
                    System.out.println("matcher.find() = " + m.group(1));
                    format = format.replace(m.group(0), "");
                    pattern.applyPattern(format);
                    return "<font style=\"color: " + m.group(1) + ";\">" + pattern.format(dvalue) + "</font>";
                }
                pattern.applyPattern(format);
                return pattern.format(dvalue);
            }
            return "other";
        } catch (Exception e) {
            System.out.println("error");
            System.out.println(e);
            return "error";
        }
    }
}
