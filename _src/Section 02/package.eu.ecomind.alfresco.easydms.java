package eu.ecomind.alfresco.easydms;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.StringTokenizer;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.ibm.icu.text.Normalizer;

public class CreateZip extends AbstractWebScript {
	private static Log logger = LogFactory.getLog(CreateZip.class);

	private static final int BUFFER_SIZE = 1024;

	private static final String MIMETYPE_ZIP = "application/zip";
	private static final String DEFAULT_FILENAME = "documenti";
	private static final String ZIP_EXTENSION = ".zip";

	private ContentService contentService;
	private NodeService nodeService;
	private NamespaceService namespaceService;
	private DictionaryService dictionaryService;
	private StoreRef storeRef;
	private String allProperties = new String("\r\n");
	

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setStoreUrl(String url) {
		this.storeRef = new StoreRef(url);
	}

	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		allProperties="";
		String noaccentStr="";
		String [] idNodi=req.getParameterNames();
		List<String> listNodi = new ArrayList<String>();
		for (String id  : idNodi )
		{
			if(id.indexOf("ID_NODO_")!=-1)
			{
				
				listNodi.add(id.substring(id.lastIndexOf("_")+1,id.length()));
			} 
			
		}
		int d=0;
		for (String id : listNodi)
		{
			System.out.println("  " + d + " " + id);
			d++;
		}
		/*
		String nodes = req.getParameter("nodes");
		if (nodes == null || nodes.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "nodes");
		}
		 */
		/*List<String> nodeIds = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(nodes, ",");
		if (tokenizer.hasMoreTokens()) {
			while (tokenizer.hasMoreTokens()) {
				
				nodeIds.add(tokenizer.nextToken());
			}
		}
	*/
		/*
		String filename = req.getParameter("filename");
		if (filename == null || filename.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "filename");
		}*/
		GregorianCalendar gc = new GregorianCalendar();
		int anno = gc.get(Calendar.YEAR);
		int mese = gc.get(Calendar.MONTH) + 1;
		int giorno = gc.get(Calendar.DATE);
		int ore = gc.get(Calendar.HOUR)+2;
		int min = gc.get(Calendar.MINUTE);
		int sec = gc.get(Calendar.SECOND);
		
		String filename=new String(giorno+""+mese+""+anno+""+ore+""+min+""+sec);
		String nomeFilesProperties = new String("documenti.txt");
		/*
		if (nomeFilesProperties == null || nomeFilesProperties.length() == 0) {
			nomeFilesProperties= new String("documenti.txt");
			//throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "nomeFilesProperties");
		}*/
	

		/*Creo hasmap con le proprieta del modello*/
		HashMap<String, HashMap<String, PropertyDefinition>> listProps= getNameProperties(new NodeRef (storeRef,listNodi.get(0)));
		
		int i=0;
		for (String nodeId : listNodi) {
			if(i!=0)allProperties+="\r\n";
			NodeRef node = new NodeRef(storeRef, nodeId);
			getTitleProperties(node,listProps);
			i++;
		}
		
		
		try {
			res.setContentType(MIMETYPE_ZIP);
			res.setHeader("Content-Transfer-Encoding", "binary");
			res.addHeader("Content-Disposition", "attachment;filename=\"" + unAccent(filename) + ZIP_EXTENSION + "\"");

			res.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
			res.setHeader("Pragma", "public");
			res.setHeader("Expires", "0");

			createZipFile(listNodi, res.getOutputStream(), new Boolean(noaccentStr),nomeFilesProperties);
		} catch (RuntimeException e) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Errore durante la generazione del file zip");
		}
	}

	public void createZipFile(List<String> nodeIds, OutputStream os, boolean noaccent,String  nomeFilesProperties) throws IOException {

		try {
			if (nodeIds != null && !nodeIds.isEmpty()) {
				File zip = TempFileProvider.createTempFile(DEFAULT_FILENAME, ZIP_EXTENSION);
				FileOutputStream stream = new FileOutputStream(zip);
				CheckedOutputStream checksum = new CheckedOutputStream(stream, new Adler32());
				BufferedOutputStream buff = new BufferedOutputStream(checksum);
				ZipOutputStream out = new ZipOutputStream(buff);
				out.setMethod(ZipOutputStream.DEFLATED);
				out.setLevel(Deflater.BEST_COMPRESSION);

				try {
					//Inserisco file txt 
					//addTxtToZip(out);
					addTxtToZip(out,nomeFilesProperties);
					for (String nodeId : nodeIds) {
						NodeRef node = new NodeRef(storeRef, nodeId);
					
						addToZip(node, out, noaccent, "");
					}
				} catch (Exception e) {
					throw new WebScriptException(
							HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
				} finally {
					out.close();
					buff.close();
					checksum.close();
					stream.close();

					if (nodeIds.size() > 0) {
						InputStream in = new FileInputStream(zip);

						byte[] buffer = new byte[BUFFER_SIZE];
						int len;

						while ((len = in.read(buffer)) > 0) {
							os.write(buffer, 0, len);
						}
					}

					zip.delete();
				}
			}
		} catch (Exception e) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}

	public void addToZip(NodeRef node, ZipOutputStream out, boolean noaccent, String path) throws IOException {
		QName nodeQnameType = this.nodeService.getType(node);
		String nodeName = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);

		//getTitleProperties(node,listProps);
		
		nodeName = noaccent ? unAccent(nodeName) : nodeName;

		if (this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_CONTENT)) {
			ContentReader reader = contentService.getReader(node, ContentModel.PROP_CONTENT);
			if (reader != null) {
				InputStream is = reader.getContentInputStream();

				String filename = path.isEmpty() ? nodeName : path + '/' + nodeName;

				ZipEntry entry = new ZipEntry(filename);
				entry.setTime(((Date) nodeService.getProperty(node, ContentModel.PROP_MODIFIED)).getTime());

				entry.setSize(reader.getSize());
				out.putNextEntry(entry);

				byte buffer[] = new byte[BUFFER_SIZE];
				while (true) {
					int nRead = is.read(buffer, 0, buffer.length);
					if (nRead <= 0) {
						break;
					}

					out.write(buffer, 0, nRead);
				}
				is.close();
				out.closeEntry();
			}
			else {
				logger.warn("Could not read : "	+ nodeName + "content");
			}
		}
		else if(this.dictionaryService.isSubClass(nodeQnameType, ContentModel.TYPE_FOLDER)) {
			List<ChildAssociationRef> children = nodeService.getChildAssocs(node);
			for (ChildAssociationRef childAssoc : children) {
				NodeRef childNodeRef = childAssoc.getChildRef();

				addToZip(childNodeRef, out, noaccent, path.isEmpty() ? nodeName : path + '/' + nodeName);
			}
		}
		else {
			logger.info("Unmanaged type: "	+ nodeQnameType.getPrefixedQName(this.namespaceService) + ", filename: " + nodeName);
		}
	}
	/**
	 * ZipEntry() does not convert filenames from Unicode to platform (waiting Java 7)
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4244499
	 * 
	 * @param s
	 * @return
	 */
	public static String unAccent(String s) {
		String temp = Normalizer.normalize(s, Normalizer.NFD, 0);
		return temp.replaceAll("[^\\p{ASCII}]", "");
	}
	
	public HashMap<String, HashMap<String, PropertyDefinition>> getNameProperties(NodeRef node)
	{
		QName nodeQnameType = this.nodeService.getType(node);
		
		return ModelUtils.getAllTypes(nodeQnameType.getNamespaceURI());
	}
	
	public void getTitleProperties(NodeRef node,HashMap<String, HashMap<String, PropertyDefinition>> listProps)
	{
		String propertiesCurrNode="";
		QName nodeQnameType = this.nodeService.getType(node);
		
		
		Map<QName, Serializable> nodeName =  nodeService.getProperties(node);
		String nomeFile = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
		System.out.println("Nome Documento"  + nomeFile);
		allProperties+="Nome documento: "+nomeFile+"\r\n";
		for(Map.Entry <QName,Serializable>  pp:    nodeName.entrySet())
		{
		
		 //System.out.println("Chiavi1  " + pp.getKey()  + " Valori "  +pp.getValue() );
		 for(String ss: listProps.keySet())
		 {
			for(Entry<String, PropertyDefinition> tt: listProps.get(ss).entrySet())
			 {
				// System.out.println("CONFRONTO " +tt.getKey()+ " ED  "+pp.getKey());
				 	if((tt.getKey().toString()).equals(pp.getKey().toString()))
				 	{
				 		if((pp.getKey().toString()).indexOf(nodeQnameType.getNamespaceURI().toString())!=-1)
				 		{			
				 			if(pp.getValue()!=null)
				 			{
				 				if(tt.getValue().getDescription()!=null)
				 				{
				 					if(propertiesCurrNode.indexOf(tt.getValue().getDescription())==-1)
				 					{
				 						allProperties+="" +tt.getValue().getDescription() +": " +pp.getValue()+"\r\n";
				 						propertiesCurrNode+="" +tt.getValue().getDescription() +": " +pp.getValue()+"\r\n";
				 						//System.out.println("" +tt.getValue().getDescription() +": " +pp.getValue());
				 					}
				 				}
				 				else
				 				{
				 					if(propertiesCurrNode.indexOf(tt.getValue().getTitle())==-1)
				 					{
				 						allProperties+="" +tt.getValue().getTitle() +": " +pp.getValue()+"\r\n";
				 						propertiesCurrNode+="" +tt.getValue().getTitle() +": " +pp.getValue()+"\r\n";
				 						//System.out.println("" +tt.getValue().getTitle() +": " +pp.getValue());
				 					}
				 				}
				 			}
				 		}
				 	}	
			 	}
		 	}
		}
	}
	
	public void addTxtToZip(ZipOutputStream out,String nomeFilesProperties) throws IOException
	{
		ZipEntry entry = new ZipEntry(nomeFilesProperties);
		 InputStream is = new ByteArrayInputStream(allProperties.getBytes("UTF-8"));
	
			out.putNextEntry(entry);
			
		
		byte buffer[] = new byte[BUFFER_SIZE];
		while (true) {
			int nRead;
			
				nRead = is.read(buffer, 0, buffer.length);
	
			if (nRead <= 0) {
				break;
			}

			out.write(buffer, 0, nRead);
		}
		is.close();
		out.closeEntry();
		
	}
	}
