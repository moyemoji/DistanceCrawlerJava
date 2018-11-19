import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

public class DistanceCrawler {
	private String result_temp = "";    // 搜索结果缓存
	private String base_path = "";      // 基础路径
	private String template_path = "";  // 模板路径
	private WebDriver browser;          // 浏览器driver
	
	/**
	 * DistanceCrawler: 构造函数
	 * @return 
	 */
	public DistanceCrawler() {
		System.out.println("------init the crawler------");
		createTemplate();  // 创建html模板文件
		this.base_path = (System.getProperty("user.dir")).replaceAll("\\\\", "/");
		this.template_path = "file:///" + base_path + "/index.html";
		this.browser = createBrowser();
		this.browser.get(this.template_path);
		System.out.println("------init the crawler, done------");
	}
	
	/**
	 * createBrowser: 初始化浏览器driver
	 * @return
	 */
	private WebDriver createBrowser() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		return new ChromeDriver(options);
	}
	
	/**
	 * createTemplate: 生成模板函数
	 * @return
	 */
	private void createTemplate() {
		StringBuilder strbd = new StringBuilder();
		PrintStream printStream = null ;
		try {
			printStream= new PrintStream(new FileOutputStream("index.html"));//路径默认在项目根目录下
		} catch (FileNotFoundException e) {
			e.printStackTrace();
        }
		strbd.append("<!DOCTYPE html>\n");
		strbd.append("<html>\n");
		strbd.append("<head>");
		strbd.append("<title>计算驾车时间与距离</title>\n");
		strbd.append("<style>\n");
		strbd.append("body,html,#allmap{width:100%;height:100%;overflow:hidden;margin:0;font-family:\"微软雅黑\";}\n");
		strbd.append(".btn-container{position:absolute;right:30px;top:30px;}\n");
		strbd.append(".btn-container button {outline: none;border: 2px solid #000;background-color: #000;color: #fff;\n");
		strbd.append("padding: 10px 15px;cursor: pointer;box-shadow: 0px 0px 3px #555;margin-right: 10px;font-weight: 600;}\n");
		strbd.append(".btn-container input{border: 2px solid #000;box-shadow: 0px 0px 3px #555;margin-right: 10px;\n");
		strbd.append("font-weight: 600;padding: 10px 15px;}\n");
		strbd.append(".btn-container button:hover {background-color: #fff;color: #000;}\n");
		strbd.append("</style>\n");
		strbd.append("<script type=\"text/javascript\" src=\"http://api.map.baidu.com/api?v=2.0&ak=GNze796xUWAyke2lcpfAsjoA5Tmo0Ijh\"></script>\n");
		strbd.append("</head>\n");
		strbd.append("<body>\n");
		strbd.append("<div id=\"allmap\"></div>\n");
		strbd.append("<div class='btn-container'>\n");
		strbd.append("<button id='btnGetDistance'>开始获取距离及持续时间</button>\n");
		strbd.append("<input id='dataInput' type='text'>\n");
		strbd.append("<input id='resultInput' type='text'>\n");
		strbd.append("</div>\n");
		strbd.append("</body>\n");
		strbd.append("</html>\n");
		strbd.append("<script type=\"text/javascript\">\n");
		strbd.append("var map = new BMap.Map(\"allmap\");\n");
		strbd.append("map.centerAndZoom(new BMap.Point(113.3176, 23.1123), 12);\n");
		strbd.append("map.enableScrollWheelZoom();\n");
		strbd.append("var searchComplete = function(results) {");
		strbd.append("if (transit.getStatus() != BMAP_STATUS_SUCCESS) { return }\n");
		strbd.append("var plan = results.getPlan(0);\n");
		strbd.append("var distance = plan.getDistance(false);\n");
		strbd.append("var duration = plan.getDuration(false);\n");
		strbd.append("updatePage(distance, duration);}\n");
		strbd.append("var transit = new BMap.DrivingRoute(map, {\n");
		strbd.append("renderOptions: {map: map},\n");
		strbd.append("onSearchComplete: searchComplete,\n");
		strbd.append("onMarkersSet: function(routes) {\n");
		strbd.append("map.removeOverlay(routes[0].marker);\n");
		strbd.append("map.removeOverlay(routes[1].marker);}\n");
		strbd.append("});\n");
		strbd.append("function getDistance(startid, endid, lonstart, latstart, lonend, latend) {\n");
		strbd.append("var p1 = new BMap.Point(lonstart, latstart);\n");
		strbd.append("var p2 = new BMap.Point(lonend, latend);\n");
		strbd.append("transit.search(p1, p2);}\n");
		strbd.append("function updatePage(distance, duration){\n");
		strbd.append("var results_str = \"{'startid':\"+startid+\", 'endid':\"+endid+\", 'distance':\"+distance+\", 'duration':\"+duration+\"}\";\n");
		strbd.append("document.getElementById(\"resultInput\").value = results_str;}\n");
		strbd.append("btnGetDistance.onclick = function() {\n");
		strbd.append("var dataInput = document.getElementById(\"dataInput\").value;\n");
		strbd.append("var dataInput = JSON.parse(dataInput);\n");
		strbd.append("startid = dataInput.startid;\n");
		strbd.append("endid = dataInput.endid;\n");
		strbd.append("lonstart = dataInput.lonstart;\n");
		strbd.append("latstart = dataInput.latstart;\n");
		strbd.append("lonend = dataInput.lonend;\n");
		strbd.append("latend = dataInput.latend;\n");
		strbd.append("if(lonstart==lonend && latstart==latend){updatePage(0,0)}\n");
		strbd.append("else{getDistance(startid, endid, lonstart, latstart, lonend, latend);}}\n");
		strbd.append("</script>\n");
		printStream.println(strbd.toString());
	}
	
	/**
	 * getDistance: 获取通行距离函数
	 * @param String query_json_str: 起终点id及经纬度，JSON字符串格式
	 * @return String val_result: 起终点id及通行距离，JSON字符串格式
	 */
	public String getDistance(String query_json_str) {
		WebElement elem_data = this.browser.findElement(By.id("dataInput"));
		WebElement elem_btn = this.browser.findElement(By.id("btnGetDistance"));
		WebElement elem_result = this.browser.findElement(By.id("resultInput"));
		Actions inputaction = new Actions(this.browser);
		
		// 页面模拟用户操作
        elem_data.clear();
        inputaction.sendKeys(query_json_str).perform();
        elem_btn.click();
        String val_result = elem_result.getAttribute("value");
        
        // 如果结果输入框为空或者与上次搜索结果一致，继续搜索
        while(val_result == "" || val_result == null || val_result.isEmpty() || val_result.equals(this.result_temp)) {
        	val_result = elem_result.getAttribute("value");
        }
        
        // 将缓存结果更新为最新搜索结果
        this.result_temp = val_result;
		return val_result;
	}
	
	/**
	 * stopBrowser: 关闭浏览器函数，释放资源
	 */
	public void stopBrowser() {
		this.browser.close();
		System.out.println("------crawler stopped------");
	}
	
	public static void main(String[] args) {
		DistanceCrawler dc = new DistanceCrawler();
		String[] dataset = {"{\"startid\":1, \"endid\":2, \"lonstart\":113.41, \"latstart\":29.58, \"lonend\":113.40, \"latend\":29.50}",
				"{\"startid\":3, \"endid\":4, \"lonstart\":113.41, \"latstart\":29.58, \"lonend\":113.40, \"latend\":29.51}"};
		for(int i=0;i<dataset.length;i++) {
			String	data = dataset[i];
			String result = dc.getDistance(data);
			System.out.println(data);
			System.out.println(result);
		}
	}
}
