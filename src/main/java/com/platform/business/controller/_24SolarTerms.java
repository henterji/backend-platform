package com.platform.business.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author:518ad-ccn date:Dec 13, 2011 describe：24节气
 *                   注：程序中使用到的计算节气公式、节气世纪常量等相关信息参照http
 *                   ://www.360doc.com/content/11
 *                   /0106/22/5281066_84591519.shtml，
 *                   程序的运行得出的节气结果绝大多数是正确的，有少数部份是有误差的
 */
public class _24SolarTerms {
	private static final double D = 0.2422;
	private final static Map<String, Integer[]> INCREASE_OFFSETMAP = new HashMap<String, Integer[]>();// +1偏移
	private final static Map<String, Integer[]> DECREASE_OFFSETMAP = new HashMap<String, Integer[]>();// -1偏移

	/** 24节气 **/
	private static enum SolarTermsEnum {
		LICHUN, // --立春
		YUSHUI, // --雨水
		JINGZHE, // --惊蛰
		CHUNFEN, // 春分
		QINGMING, // 清明
		GUYU, // 谷雨
		LIXIA, // 立夏
		XIAOMAN, // 小满
		MANGZHONG, // 芒种
		XIAZHI, // 夏至
		XIAOSHU, // 小暑
		DASHU, // 大暑
		LIQIU, // 立秋
		CHUSHU, // 处暑
		BAILU, // 白露
		QIUFEN, // 秋分
		HANLU, // 寒露
		SHUANGJIANG, // 霜降
		LIDONG, // 立冬
		XIAOXUE, // 小雪
		DAXUE, // 大雪
		DONGZHI, // 冬至
		XIAOHAN, // 小寒
		DAHAN;// 大寒
	}

	static {
		DECREASE_OFFSETMAP.put(SolarTermsEnum.YUSHUI.name(),
				new Integer[] { 2026 });// 雨水
		INCREASE_OFFSETMAP.put(SolarTermsEnum.CHUNFEN.name(),
				new Integer[] { 2084 });// 春分
		INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAOMAN.name(),
				new Integer[] { 2008 });// 小满
		INCREASE_OFFSETMAP.put(SolarTermsEnum.MANGZHONG.name(),
				new Integer[] { 1902 });// 芒种
		INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAZHI.name(),
				new Integer[] { 1928 });// 夏至
		INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAOSHU.name(), new Integer[] {
				1925, 2016 });// 小暑
		INCREASE_OFFSETMAP.put(SolarTermsEnum.DASHU.name(),
				new Integer[] { 1922 });// 大暑
		INCREASE_OFFSETMAP.put(SolarTermsEnum.LIQIU.name(),
				new Integer[] { 2002 });// 立秋
		INCREASE_OFFSETMAP.put(SolarTermsEnum.BAILU.name(),
				new Integer[] { 1927 });// 白露
		INCREASE_OFFSETMAP.put(SolarTermsEnum.QIUFEN.name(),
				new Integer[] { 1942 });// 秋分
		INCREASE_OFFSETMAP.put(SolarTermsEnum.SHUANGJIANG.name(),
				new Integer[] { 2089 });// 霜降
		INCREASE_OFFSETMAP.put(SolarTermsEnum.LIDONG.name(),
				new Integer[] { 2089 });// 立冬
		INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAOXUE.name(),
				new Integer[] { 1978 });// 小雪
		INCREASE_OFFSETMAP.put(SolarTermsEnum.DAXUE.name(),
				new Integer[] { 1954 });// 大雪
		DECREASE_OFFSETMAP.put(SolarTermsEnum.DONGZHI.name(), new Integer[] {
				1918, 2021 });// 冬至

		INCREASE_OFFSETMAP.put(SolarTermsEnum.XIAOHAN.name(),
				new Integer[] { 1982 });// 小寒
		DECREASE_OFFSETMAP.put(SolarTermsEnum.XIAOHAN.name(),
				new Integer[] { 2019 });// 小寒

		INCREASE_OFFSETMAP.put(SolarTermsEnum.DAHAN.name(),
				new Integer[] { 2082 });// 大寒
	}

	// 定义一个二维数组，第一维数组存储的是20世纪的节气C值，第二维数组存储的是21世纪的节气C值,0到23个，依次代表立春、雨水...大寒节气的C值
	private static final double[][] CENTURY_ARRAY = {
			{ 4.6295, 19.4599, 6.3826, 21.4155, 5.59, 20.888, 6.318, 21.86,
					6.5, 22.2, 7.928, 23.65, 8.35, 23.95, 8.44, 23.822, 9.098,
					24.218, 8.218, 23.08, 7.9, 22.6, 6.11, 20.84 },
			{ 3.87, 18.73, 5.63, 20.646, 4.81, 20.1, 5.52, 21.04, 5.678, 21.37,
					7.108, 22.83, 7.5, 23.13, 7.646, 23.042, 8.318, 23.438,
					7.438, 22.36, 7.18, 21.94, 5.4055, 20.12 } };

	/**
	 * 
	 * @param year
	 *            年份
	 * @param name
	 *            节气的名称
	 * @return 返回节气是相应月份的第几天
	 */
	public static int getSolarTermNum(int year, String name) {

		double centuryValue = 0;// 节气的世纪值，每个节气的每个世纪值都不同
		name = name.trim().toUpperCase();
		int ordinal = SolarTermsEnum.valueOf(name).ordinal();

		int centuryIndex = -1;
		if (year >= 1901 && year <= 2000) {// 20世纪
			centuryIndex = 0;
		} else if (year >= 2001 && year <= 2100) {// 21世纪
			centuryIndex = 1;
		} else {
			throw new RuntimeException("不支持此年份：" + year
					+ "，目前只支持1901年到2100年的时间范围");
		}
		centuryValue = CENTURY_ARRAY[centuryIndex][ordinal];
		int dateNum = 0;
		/**
		 * 计算 num =[Y*D+C]-L这是传说中的寿星通用公式
		 * 公式解读：年数的后2位乘0.2422加C(即：centuryValue)取整数后，减闰年数
		 */
		int y = year % 100;// 步骤1:取年分的后两位数
		if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {// 闰年
			if (ordinal == SolarTermsEnum.XIAOHAN.ordinal()
					|| ordinal == SolarTermsEnum.DAHAN.ordinal()
					|| ordinal == SolarTermsEnum.LICHUN.ordinal()
					|| ordinal == SolarTermsEnum.YUSHUI.ordinal()) {
				// 注意：凡闰年3月1日前闰年数要减一，即：L=[(Y-1)/4],因为小寒、大寒、立春、雨水这两个节气都小于3月1日,所以
				// y = y-1
				y = y - 1;// 步骤2
			}
		}
		dateNum = (int) (y * D + centuryValue) - (int) (y / 4);// 步骤3，使用公式[Y*D+C]-L计算
		dateNum += specialYearOffset(year, name);// 步骤4，加上特殊的年分的节气偏移量
		return dateNum;
	}

	/**
	 * 特例,特殊的年分的节气偏移量,由于公式并不完善，所以算出的个别节气的第几天数并不准确，在此返回其偏移量
	 * 
	 * @param year
	 *            年份
	 * @param name
	 *            节气名称
	 * @return 返回其偏移量
	 */
	public static int specialYearOffset(int year, String name) {
		int offset = 0;
		offset += getOffset(DECREASE_OFFSETMAP, year, name, -1);
		offset += getOffset(INCREASE_OFFSETMAP, year, name, 1);

		return offset;
	}

	public static int getOffset(Map<String, Integer[]> map, int year,
			String name, int offset) {
		int off = 0;
		Integer[] years = map.get(name);
		if (null != years) {
			for (int i : years) {
				if (i == year) {
					off = offset;
					break;
				}
			}
		}
		return off;
	}

    public final static String[] solarTerm = {
        "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
        "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
        "小暑", "大暑", "立秋", "处暑", "白露", "秋分",
        "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    };
    public final static String[] solarTerm3 = {
    	"小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
    	"清明", "谷雨", "立夏", "小满", "芒种", "夏至",
    	"小暑", "大暑", "立秋", "处暑", "白露", "秋分",
    	"寒露", "霜降", "立冬", "小雪", "大雪", "冬至",
    	"小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
    	"清明", "谷雨", "立夏", "小满", "芒种", "夏至",
    	"小暑", "大暑", "立秋", "处暑", "白露", "秋分",
    	"寒露", "霜降", "立冬", "小雪", "大雪", "冬至",
    	"小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
    	"清明", "谷雨", "立夏", "小满", "芒种", "夏至",
    	"小暑", "大暑", "立秋", "处暑", "白露", "秋分",
    	"寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    };
    public final static String[] solarTermName = {
    	"XIAOHAN", "DAHAN", "LICHUN", "YUSHUI", "JINGZHE", "CHUNFEN",
    	"QINGMING", "GUYU", "LIXIA", "XIAOMAN", "MANGZHONG", "XIAZHI",
    	"XIAOSHU", "DASHU", "LIQIU", "CHUSHU", "BAILU", "QIUFEN",
    	"HANLU", "SHUANGJIANG", "LIDONG", "XIAOXUE", "DAXUE", "DONGZHI"
    };
    public final static int[] month = {
    	0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11
    };
	
	public static String solarTermToString(int year) {
		StringBuffer sb = new StringBuffer();
		sb.append("---").append(year);
		if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {// 闰年
			sb.append(" 闰年");
		} else {
			sb.append(" 平年");
		}

		sb.append("\n")
				.append("小寒:1月")
				.append(getSolarTermNum(year, SolarTermsEnum.XIAOHAN.name()))
				.append("日,大寒:1月")
				.append(getSolarTermNum(year, SolarTermsEnum.DAHAN.name()))
				.append("立春：2月")
				.append(getSolarTermNum(year, SolarTermsEnum.LICHUN.name()))
				.append("日,雨水：2月")
				.append(getSolarTermNum(year, SolarTermsEnum.YUSHUI.name()))
				.append("日,惊蛰:3月")
				.append(getSolarTermNum(year, SolarTermsEnum.JINGZHE.name()))
				.append("日,春分:3月")
				.append(getSolarTermNum(year, SolarTermsEnum.CHUNFEN.name()))
				.append("日,清明:4月")
				.append(getSolarTermNum(year, SolarTermsEnum.QINGMING.name()))
				.append("日,谷雨:4月")
				.append(getSolarTermNum(year, SolarTermsEnum.GUYU.name()))
				.append("日,立夏:5月")
				.append(getSolarTermNum(year, SolarTermsEnum.LIXIA.name()))
				.append("日,小满:5月")
				.append(getSolarTermNum(year, SolarTermsEnum.XIAOMAN.name()))
				.append("日,芒种:6月")
				.append(getSolarTermNum(year, SolarTermsEnum.MANGZHONG.name()))
				.append("日,夏至:6月")
				.append(getSolarTermNum(year, SolarTermsEnum.XIAZHI.name()))
				.append("日,小暑:7月")
				.append(getSolarTermNum(year, SolarTermsEnum.XIAOSHU.name()))
				.append("日,大暑:7月")
				.append(getSolarTermNum(year, SolarTermsEnum.DASHU.name()))
				.append("日,\n立秋:8月")
				.append(getSolarTermNum(year, SolarTermsEnum.LIQIU.name()))
				.append("日,处暑:8月")
				.append(getSolarTermNum(year, SolarTermsEnum.CHUSHU.name()))
				.append("日,白露:9月")
				.append(getSolarTermNum(year, SolarTermsEnum.BAILU.name()))
				.append("日,秋分:9月")
				.append(getSolarTermNum(year, SolarTermsEnum.QIUFEN.name()))
				.append("日,寒露:10月")
				.append(getSolarTermNum(year, SolarTermsEnum.HANLU.name()))
				.append("日,霜降:10月")
				.append(getSolarTermNum(year, SolarTermsEnum.SHUANGJIANG.name()))
				.append("日,立冬:11月")
				.append(getSolarTermNum(year, SolarTermsEnum.LIDONG.name()))
				.append("日,小雪:11月")
				.append(getSolarTermNum(year, SolarTermsEnum.XIAOXUE.name()))
				.append("日,大雪:12月")
				.append(getSolarTermNum(year, SolarTermsEnum.DAXUE.name()))
				.append("日,冬至:12月")
				.append(getSolarTermNum(year, SolarTermsEnum.DONGZHI.name()))
				.append("日");

		return sb.toString();
	}
    /**
     * 获得某年中所有节气Date
     *
     * @return
     */
    public static Date[] jieqilist(int year) {
        Date[] returnvalue = new Date[solarTerm.length];
        Calendar t = Calendar.getInstance();
        for (int i = 0; i < solarTerm.length; i++) {
        	t.clear();
        	t.set(year, month[i], getSolarTermNum(year, solarTermName[i]));
            
            returnvalue[i] = t.getTime();

        }
        return returnvalue;
    }
    
    public static List<Date> Alljieqi(int year)
    {
    	
    	List<Date> jieqi = new  ArrayList<Date>();
    	Date[] temp ;
    	temp = jieqilist(year - 1);
    	jieqi.addAll(Arrays.asList(temp));
    	temp = jieqilist(year);
    	jieqi.addAll(Arrays.asList(temp));
    	temp = jieqilist(year+1);
    	jieqi.addAll(Arrays.asList(temp));
    	return jieqi;
    }
    /**
     * 获得某天前后两个节气序号
     *
     * @return
     */
    public static int[] getnearsolarTerm(int year, Date date) {
      List<Date> jieqi =Alljieqi(year);
        System.out.println("+++++++++++++++++++++++++++" + sdf.format(date));
        int[] returnValue = new int[2];
        for (int i = 0; i < jieqi.size(); i++) {
            if (date.getTime() > jieqi.get(i).getTime()) {
                continue;
            }
            System.out.println("+++++++++++++++++++++++++++" + sdf.format(jieqi.get(i)));
            returnValue[0] = i - 1;
            returnValue[1] = i;
//            if (i % 2 == 0) {//只管气
//                returnValue[0] = i - 2;
//                returnValue[1] = i;
//            } else {
//                returnValue[0] = i - 1;
//                returnValue[1] = i + 1;
//
//            }
            break;
        }

        return returnValue;
    }

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

	public static void main(String[] args) {
		Calendar t = Calendar.getInstance();
		t.clear();
		t.set(2013, 7 - 1, 23);
		List<Date> jieqi = Alljieqi(2013);

		int[] jieqibeforeafter = getnearsolarTerm(2013, t.getTime());
		System.out.println(sdf.format(t.getTime()));

		int b = Integer.parseInt(String.valueOf(MyDate.DateDays(t.getTime(),
				jieqi.get(jieqibeforeafter[0])))) + 1;
		int a = Integer.parseInt(String.valueOf(MyDate.DateDays(t.getTime(),
				jieqi.get(jieqibeforeafter[1]))));

		String result = "";
		if (a == 0) {
			result += solarTerm3[jieqibeforeafter[1]];
		} else if (b <= 8) {
			result += solarTerm3[jieqibeforeafter[0]] + "第" + b + "天";
		} else {
			result += solarTerm3[jieqibeforeafter[1]] + "还有" + a + "天";
		}
		System.out.println(result);
	}
//	public static void main(String[] args) {
//		Calendar t = Calendar.getInstance();
//		t.clear();
//		t.set(2014, 8-1, 19);
//		for (int year = 1901; year < 2050; year++) {
//		}
//		System.out.println(solarTermToString(2013));
//		List<Date> jieqi = Alljieqi(2014);
//		
//		for (int i = 0; i < jieqi.size(); i++) {
//			System.out.println(sdf.format(jieqi.get(i)));
//		}
//		
//		int[] jieqibeforeafter = getnearsolarTerm(2014, t.getTime());
//		System.out.println(sdf.format(t.getTime()));
//		
//		
//		int b = Integer.parseInt(String.valueOf(MyDate.DateDays(t.getTime(), jieqi.get(jieqibeforeafter[0])))) + 1;
//		int a = Integer.parseInt(String.valueOf(MyDate.DateDays(t.getTime(), jieqi.get(jieqibeforeafter[1]))));
//		
//		System.out.println("今天是 " + sdf1.format(t.getTime()));
//		System.out.println(b);
//		System.out.println(sdf.format(jieqi.get(jieqibeforeafter[0])));
//		System.out.println(new Lunar(jieqi.get(jieqibeforeafter[0]).getTime()).getTermString());
//		System.out.println(a);
//		System.out.println(sdf.format(jieqi.get(jieqibeforeafter[1])));
//		System.out.println(new Lunar(jieqi.get(jieqibeforeafter[1]).getTime()).getTermString());
//		
//		String result = "";
//		if(a == 0) {
//			result += new Lunar(jieqi.get(jieqibeforeafter[0]).getTime()).getTermString();
//		}else if(b <= 8) {
//			result += new Lunar(jieqi.get(jieqibeforeafter[0]).getTime()).getTermString() + "第" + b + "天";
//		} else {
//			result += "距离" + new Lunar(jieqi.get(jieqibeforeafter[1]).getTime()).getTermString() + "还有" + a + "天";
//		}
//		System.out.println(result);
//	}
	
	
}