package com.jaha.server.emaul.model.xml;

import com.google.common.collect.Lists;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by doring on 15. 5. 15..
 */
@Deprecated
public class ItemListGenerator {

    public static <T> List<T> create(String xmlData, Class<T> classOfT) throws ParserConfigurationException,
            IOException, SAXException, XPathExpressionException, IllegalAccessException, InstantiationException {

        if (xmlData == null) {
            return null;
        }
        List<T> ret = Lists.newArrayList();

        InputSource is = new InputSource(new StringReader(xmlData));
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        XPath xPath = XPathFactory.newInstance().newXPath();

        NodeList itemList = (NodeList) xPath.evaluate("//itemList", document, XPathConstants.NODESET);
        final int count = itemList == null ? 0 : itemList.getLength();

        for (int i = 0; i < count; i++) {
            T typeInstance = classOfT.newInstance();
            Node item = itemList.item(i);
            NodeList childNodes = item.getChildNodes();
            final int childCount = childNodes.getLength();
            for (int j = 0; j < childCount; j++) {
                Node node = childNodes.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    try {
                        Field field = classOfT.getDeclaredField(node.getNodeName());
                        field.set(typeInstance, node.getTextContent());
                    } catch (Exception e) {
                        // do nothing
                    }
                }
            }

            ret.add(typeInstance);
        }

        return ret;
    }
}
