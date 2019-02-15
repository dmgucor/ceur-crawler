import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

public class Crawler {

	private HashSet<String> links;
	private HashSet<String> papers;
	private static final int MAX_DEPTH = 3;   
	private boolean go = true;
	private static int counter = 0;

	public Crawler(){
		links = new HashSet<String>();
		papers = new HashSet<String>();
	}

	public void getPageLinks(String URL, int depth) {
		if((URL.contains("Vol-") || go) && !URL.contains("validator") && !URL.contains("preface")) {
			if(counter <= 2) { //limita el numero de rondas
				go = false;
				Long initialTime = System.currentTimeMillis();
				//4. Check if you have already crawled the URLs 
				//(we are intentionally not checking for duplicate content in this example)
				if ((!links.contains(URL) && (depth <  MAX_DEPTH))) {
					System.out.println(">> Depth: " + depth + " [" + URL + "]");
					try {
						//4. (i) If not add it to the index
						if (links.add(URL)) {
							System.out.println(URL);
						}

						if(depth == 2 && papers.add(URL) ) {
						}

						Long checkTime = System.currentTimeMillis();
						while(checkTime - initialTime < 0.00025) {
							checkTime = System.currentTimeMillis();
						}
						//2. Fetch the HTML code
						Document document = Jsoup.connect(URL).get();
						//3. Parse the HTML to extract links to other URLs
						Elements linksOnPage = document.select("a[href]");
						depth++;
						counter++;
						//5. For each extracted URL... go back to Step 4.
						for (Element page : linksOnPage) {
							getPageLinks(page.attr("abs:href"), depth);
						}
					} catch (IOException e) {	
						System.err.println("For '" + URL + "': " + e.getMessage());
					}
				}
			}
		}
	}

	public void readPDF(String pdfName) throws InvalidPasswordException, IOException {
		PDDocument doc = PDDocument.load(new File(pdfName));
		PDFTextStripper pdfStripper = new PDFTextStripper();
		pdfStripper.setStartPage(1);
		pdfStripper.setEndPage(1);
		String text = pdfStripper.getText(doc);
		System.out.println(text);
		doc.close();
	}
	
	public void getPDF(String route) throws IOException {
		URL url = new URL(route);
		File file = new File("file.pdf");
		FileUtils.copyURLToFile(url, file);
	}

	public void writeCSV() throws InvalidPasswordException, IOException {
	for(String link : papers) {
		getPDF(link);
		break;}
	readPDF("archivo.pdf");
	//por lo pronto leer primero
	}

	public static void main(String[] args) throws InvalidPasswordException, IOException{
		//1. Pick a URL from the frontier
		Crawler crawler = new Crawler();
		crawler.getPageLinks("http://ceur-ws.org/",0);
		crawler.writeCSV();
	}

}