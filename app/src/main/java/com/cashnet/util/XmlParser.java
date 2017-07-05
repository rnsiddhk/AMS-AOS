package com.cashnet.util;

//---------------------------------------------------------------------------------------------------
// Library
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;

//---------------------------------------------------------------------------------------------------
// zXmlParser
public class XmlParser
{
	//---------------------------------------------------------------------------------------------------
	// Member
	private boolean m_bInit = false;
	public boolean IsInit(){ return m_bInit; }
	
	private DocumentBuilderFactory m_DomFactory = null;
	public DocumentBuilder m_DomBuilder = null;

	private XmlPullParserFactory m_PullFactory = null;
	public XmlSerializer m_Serializer = null;
	
	private StringWriter m_stReqXmlResult = null;
	private String m_strRootNodeName = "";

	//---------------------------------------------------------------------------------------------------
	// Creator
	public XmlParser()
	{
		init();
	}
	
	//---------------------------------------------------------------------------------------------------
	// Initialize
	public void init()
	{
		if( m_bInit )
			return;
		
		try
		{
			m_DomFactory = DocumentBuilderFactory.newInstance();
			m_DomBuilder = m_DomFactory.newDocumentBuilder();

			m_PullFactory = XmlPullParserFactory.newInstance();
			m_PullFactory.setNamespaceAware(true);
			m_Serializer = m_PullFactory.newSerializer();
			
			m_bInit = true;
		}
		catch(XmlPullParserException e)
		{
			m_PullFactory = null;
			m_Serializer = null;
		}
		catch(ParserConfigurationException e)
		{
			m_DomFactory = null;
			m_DomBuilder = null;
		}
	}
	
	//---------------------------------------------------------------------------------------------------
	// UnInitialize
	public void uninit()
	{
		if( m_bInit == false )
			return;
		
		m_DomFactory = null;
		m_DomBuilder = null;

		m_PullFactory = null;
		m_Serializer = null;
		
		m_bInit = false;
	}

	//////////////////////////////////////// Build Function ////////////////////////////////////////
	//---------------------------------------------------------------------------------------------------
	// Start Document
	public boolean startDocument(String strRootName, String strTextEncoding)
	{
		if( m_bInit == false || strRootName == null || strRootName == "" )
			return false;
		
		try
		{
			// Set Output Stream
			if( m_stReqXmlResult != null )
				m_stReqXmlResult = null;
			m_stReqXmlResult = new StringWriter();
			m_Serializer.setOutput(m_stReqXmlResult);

			// Start Document
			if( strTextEncoding == null || strTextEncoding == "" )
				strTextEncoding = "UTF-8";
			m_Serializer.startDocument(strTextEncoding, false);
			
			// Start Root Node
			m_strRootNodeName = strRootName;
			m_Serializer.startTag(null, m_strRootNodeName);
			
			return true;
			
		}
		catch(IOException e)
		{
			return false;
		}
	}
	
	//---------------------------------------------------------------------------------------------------
	// End Document
	public String endDocument()
	{
		// End
		if( m_bInit == false || m_stReqXmlResult == null )
			return "";
		
		try
		{
			m_Serializer.endTag(null, m_strRootNodeName);

			m_Serializer.endDocument();
			m_Serializer.flush();
			
			return m_stReqXmlResult.toString();
		}
		catch(IOException e)
		{
			return "";
		}
	}
	
	//---------------------------------------------------------------------------------------------------
	// Start Tag
	public boolean startTag(String strTagName)
	{
		if( m_bInit == false || strTagName == null || strTagName == "" )
			return false;
		
		try
		{
			m_Serializer.startTag("", strTagName);
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
	}
	
	//---------------------------------------------------------------------------------------------------
	// End Tag
	public boolean endTag(String strTagName)
	{
		if( m_bInit == false || strTagName == null || strTagName == "" )
			return false;
		
		try
		{
			m_Serializer.endTag("", strTagName);
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
	}
	
	//---------------------------------------------------------------------------------------------------
	// Create Text Node
	public boolean createNode(String strName, String strText, LinkedHashMap<String, String> stAttrList)
	{
		if( m_bInit == false || strName == null || strName == "" || strText == null )
			return false;
		
		try
		{
			boolean bResult = false;
			
			// Start Node
			m_Serializer.startTag(null, strName);
			
			// Set Node Text
			m_Serializer.text(strText);
			
			// Attribute
			if( stAttrList == null )
				bResult = true;
			else
			{
				Iterator<String> iter = stAttrList.keySet().iterator();
				while( iter.hasNext() )
				{
					bResult = false;
					String strAttrName = iter.next();
					String strAttrValue = stAttrList.get(strName);

					if( createAttr(strAttrName, strAttrValue) == false )
						break;
					
					bResult = true;
				}
			}
			
			// End Node
			m_Serializer.endTag(null, strName);
			
			return bResult;
		}
		catch(IOException e)
		{
			return false;
		}
	}
	
	//---------------------------------------------------------------------------------------------------
	// Create Attribute
	public boolean createAttr(String strAttrName, String strAttrValue)
	{
		if( m_bInit == false || strAttrName == null || strAttrName == "" || strAttrValue == null )
			return false;
		
		try
		{
			m_Serializer.attribute("", strAttrName, strAttrValue);
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
	}
	
	//---------------------------------------------------------------------------------------------------
	// Create Text Node 
	public boolean createTextNode(String strName, String strText)
	{
		return createNode(strName, strText, null);
	}

	//---------------------------------------------------------------------------------------------------
	// Create Text Node With Attribute
	public boolean createTextNode(String strName, String strText, LinkedHashMap<String, String> stAttrList)
	{
		return createNode(strName, strText, stAttrList);
	}
	
	//---------------------------------------------------------------------------------------------------
	// Create Integer Node
	public boolean createIntNode(String strName, int nNo)
	{
		String strText = "" + nNo;
		return createTextNode(strName, strText, null);
	}

	//---------------------------------------------------------------------------------------------------
	// Create Integer Node With Attribute
	public boolean createIntNode(String strName, int nNo, LinkedHashMap<String, String> stAttrList)
	{
		String strText = "" + nNo;
		return createTextNode(strName, strText, stAttrList);
	}

	//////////////////////////////////////// Parse Function ////////////////////////////////////////
	//---------------------------------------------------------------------------------------------------
	// Load XML From Input Stream & Return Root Node
	public Node loadXmlStream(InputStream stInputStream)
	{
		if( m_bInit == false || stInputStream == null )
			return null;
		
		Document stDomDocument;
		try
		{
			stDomDocument = m_DomBuilder.parse(stInputStream);

			return stDomDocument.getDocumentElement();
		}
		catch(SAXException e)
		{
			return null;
		}
		catch(IOException e)
		{
			return null;
		}
	}

	//---------------------------------------------------------------------------------------------------
	// Load XML From File & Return Root Node
	public Element loadXmlFile(File stFile)
	{
		if( m_bInit == false || stFile == null )
			return null;
		
		Document stDomDocument;
		try
		{
			stDomDocument = m_DomBuilder.parse(stFile);

			return stDomDocument.getDocumentElement();
		}
		catch(SAXException e)
		{
			return null;
		}
		catch(IOException e)
		{
			return null;
		}
	}
	
	//---------------------------------------------------------------------------------------------------
		// Load XML From File & Return Root Node
		public Element loadXmlUri(String uri)
		{
			if( m_bInit == false || uri == null )
				return null;
			
			Document stDomDocument;
			try
			{
				stDomDocument = m_DomBuilder.parse(uri);

				return stDomDocument.getDocumentElement();
			}
			catch(SAXException e)
			{
				return null;
			}
			catch(IOException e)
			{
				return null;
			}
		}
	
	//---------------------------------------------------------------------------------------------------
	// Load XML From Local File & Return Root Node
	public Element loadXmlFile(String strFilePath)
	{
		if( m_bInit == false || strFilePath == null || strFilePath == "" )
			return null;
		
		File stFile = new File(strFilePath);
		return loadXmlFile(stFile);
	}
	
	//---------------------------------------------------------------------------------------------------
	// Get Node
	public Node getNode(Node stNode, String strName)
	{
		if( stNode == null || strName == null || strName == "" )
			return null;
		
		if( stNode.hasChildNodes() == false )
			return null;
			
		NodeList stNodeList = stNode.getChildNodes();
		for(int i = 0; i < stNodeList.getLength(); i++)
		{
			Node stChildNode = stNodeList.item(i);
			if( stChildNode == null )
				continue;
			
			String strNodeName = stChildNode.getNodeName();
			if( strNodeName.compareTo(strName) == 0 )
				return stChildNode;
		}
		
		return null;
	}
	
	//---------------------------------------------------------------------------------------------------
	// Get Node
	public NodeList getNodeList(Node stNode, String strName)
	{
		if( stNode == null || strName == null || strName == "" )
			return null;
		
		if( stNode.hasChildNodes() == false )
			return null;
			
		return stNode.getChildNodes();
	}

	//---------------------------------------------------------------------------------------------------
	// Get Node Name
	public String getNodeName(Node stNode)
	{
		if( stNode == null )
			return "";

		return stNode.getNodeName();
	}

	//---------------------------------------------------------------------------------------------------
	// Get Node Text
	public String getNodeText(Node stNode)
	{
		if( stNode == null )
			return "";
		
		short nNodeType = stNode.getNodeType();

		// CData Section
		if( nNodeType == stNode.ELEMENT_NODE )
		{
			StringBuilder stText = new StringBuilder();
			NodeList stChildNodes = stNode.getChildNodes();
			
			for(int i = 0; i < stChildNodes.getLength(); i++)
			{
				stText.append(stChildNodes.item(i).getNodeValue());
			}
			
			return stText.toString();
		}

		String strNodeValue = stNode.getNodeValue();
		if( strNodeValue == null )
			return "";
		
		return strNodeValue;
	}

	//---------------------------------------------------------------------------------------------------
	// Get Node & Node Value
	public String GetNodeTextValue(Node stNode, String strNodeName)
	{
		Node stResultNode = getNode(stNode, strNodeName);
		return getNodeText(stResultNode);
	}

	//---------------------------------------------------------------------------------------------------
	// Get Node Int
	public int getNodeIntValue(Node stNode, String strNodeName)
	{
		String strText = GetNodeTextValue(stNode, strNodeName);
		if( strText == null || strText.length() == 0 )
			return -1;

		return Integer.parseInt(strText);
	}
	
	//---------------------------------------------------------------------------------------------------
	// Get Node Long
	public long getNodeLongValue(Node stNode, String strNodeName)
	{
		String strText = GetNodeTextValue(stNode, strNodeName);
		if( strText == null || strText.length() == 0 )
			return -1;

		return Long.parseLong(strText);
	}
	
	//---------------------------------------------------------------------------------------------------
	// Get Node Double
	public double getNodeDoubleValue(Node stNode, String strNodeName)
	{
		String strText = GetNodeTextValue(stNode, strNodeName);
		if( strText == null || strText.length() == 0 )
			return -1;

		return Double.parseDouble(strText);
	}

	//////////////////////////////////////// xPath ////////////////////////////////////////
	//---------------------------------------------------------------------------------------------------
	// Find First Node
	public Node findNode(Node stNode, String strName)
	{
		if( stNode == null || strName == null || strName == "" )
			return null;
		
		Element stElement = (Element)stNode;
		NodeList stNodeList = stElement.getElementsByTagName(strName);
		if( stNodeList.getLength() < 1 )
			return null;

		return stNodeList.item(0);
	}

	//---------------------------------------------------------------------------------------------------
	// Find Node List
	public NodeList findNodeList(Node stNode, String strName)
	{
		if( stNode == null || strName == null || strName == "" )
			return null;

		Element stElement = (Element)stNode;
		return stElement.getElementsByTagName(strName);
	}
	
	//---------------------------------------------------------------------------------------------------
	// Find Node & Get Text
	public String findNodeText(Node stNode, String strName)
	{
		Node stResultNode = findNode(stNode, strName);
		if( stResultNode == null )
			return "";
		
		return getNodeText(stResultNode);
	}

	//---------------------------------------------------------------------------------------------------
	// Find Node & Get Text -> Integer
	public int findNodeInt(Node stNode, String strName)
	{
		String strText = findNodeText(stNode, strName);
		if( strText == null || strText.length() == 0 )
			return -1;

		return Integer.parseInt(strText);
	}
	
	//---------------------------------------------------------------------------------------------------
	// Find Node & Get Text -> Long
	public long findNodeLong(Node stNode, String strName)
	{
		String strText = findNodeText(stNode, strName);
		if( strText == null || strText.length() == 0 )
			return -1;
		
		return Long.parseLong(strText);
	}
}
//---------------------------------------------------------------------------------------------------
//