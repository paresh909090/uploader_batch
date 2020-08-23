package com.document.batch.document.batch.parser;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParserConfig;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.sax.ExpandedTitleContentHandler;
import org.fit.pdfdom.PDFDomTree;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

@Service
public class FileParser {
	
	@Value(value = "${resources_base_location}")
	private String resourcesBaseLocation;

	@Value(value = "${html_location}")
	private String htmlLocation;
	
    public String convertDocument(String inputFile) throws IOException {
    	
    	String outputFileNameAndPath = this.generateHTMLFileAbsolutePath(inputFile);
    	
    	if(inputFile.endsWith(".pdf")) {
    		this.pdfToHTML(inputFile, outputFileNameAndPath);
    	}else {
    		this.docToHTML(inputFile, outputFileNameAndPath);
    	}
    	
        return outputFileNameAndPath;
    }
    
    private String generateHTMLFileAbsolutePath(String inputFile) {
    	Path filePath = Paths.get(inputFile);
    	String fileName = filePath.getFileName().toString();
    	
    	if(fileName.endsWith(".docx")) {
    		fileName = fileName.replace(".docx", ".html");
    	}else if(fileName.endsWith(".doc")) {
    		fileName = fileName.replace(".doc", ".html");
    	}else if(fileName.endsWith(".pdf")) {
    		fileName = fileName.replace(".pdf", ".html");
    	}
    	
    	return this.htmlLocation + fileName;
    }
    
    private String docToHTML(String fileNameAndPath, String outputFileNameAndPath) throws IOException {
    
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileNameAndPath));
        byte[] bytes = com.google.common.io.Files.toByteArray(new File(fileNameAndPath));
        
        try {
			this.convertDoc2Html(bytes, writer);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (TikaException e) {
			e.printStackTrace();
		}
    	
    	return outputFileNameAndPath;
    }

    private String pdfToHTML(String fileNameAndPath, String outputFileNameAndPath) throws IOException {
    	
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileNameAndPath));
        byte[] bytes = com.google.common.io.Files.toByteArray(new File(fileNameAndPath));
        
        try {
			this.convertPdf2Html(bytes, writer);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
        
        return outputFileNameAndPath;
    }
    
    private void convertPdf2Html(byte[] input,Writer out) throws IOException, ParserConfigurationException {
        PDDocument pdf = PDDocument.load(input);
        PDFDomTree tree = new PDFDomTree();
        tree.writeText(pdf,out);
    }
    
    public void convertDoc2Html(byte[] bytes,Writer out) throws IOException, TransformerConfigurationException, SAXException, TikaException {
    	
    	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		TransformerHandler handler = factory.newTransformerHandler();
		handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
		handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
		handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		handler.setResult(new StreamResult(outStream));
		ExpandedTitleContentHandler handler1 = new ExpandedTitleContentHandler(handler);
		  
	    Parser parser = new AutoDetectParser();
	    TesseractOCRConfig config = new TesseractOCRConfig();
	    OfficeParserConfig officeConfig = new OfficeParserConfig();
	    officeConfig.setUseSAXPptxExtractor(true);
	    ParseContext parseContext = new ParseContext();
	    parseContext.set(TesseractOCRConfig.class, config);
	    parseContext.set(OfficeParserConfig.class, officeConfig);
	    parseContext.set(Parser.class, parser); // need to add this to make sure recursive parsing happens!
	    Metadata metadata = new Metadata();
	    parser.parse(new ByteArrayInputStream(bytes), handler1, metadata, parseContext);
	    out.write(new String(outStream.toByteArray(), "UTF-8"));
		out.close();
	}
}
