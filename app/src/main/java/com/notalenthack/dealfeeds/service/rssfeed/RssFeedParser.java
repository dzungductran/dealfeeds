package com.notalenthack.dealfeeds.service.rssfeed;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.notalenthack.dealfeeds.common.Constant;
import com.notalenthack.dealfeeds.model.Product;

public class RssFeedParser extends DefaultHandler {
		private Product item;
        private RssFeedChannel channel;
        private boolean parsingChannel;
        private boolean parsingItem;
        private String xmlElementName;
        private StringBuilder builder;
        private ArrayList<Product> productList;
        
        public RssFeedParser(ArrayList<Product> pList) {
                super();
                //
                channel = new RssFeedChannel();
                builder = new StringBuilder();
                productList = pList;
        }
        
        public RssFeedChannel getChannel(){
            if (Constant.debug) Log.d(Constant.LOGTAG, "getChannel()");
            return channel;
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
                super.characters(ch, start, length);       
                builder.append(ch,start,length);
        }

        @Override
        public void endDocument() throws SAXException {      
                super.endDocument();
                //Log.d(TAG, "endDocument()");
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            String value = builder.toString();

            //Log.d("RSS_PARSER", "localName " + localName);

            if(localName.equalsIgnoreCase("item")){
        		//Log.d(TAG, "Save The Element Here");
                parsingItem = false;
                productList.add(item);
                return;
            }
            //Log.d("RSS_PARSER", "endElement: xmlElementName " + xmlElementName + " value " + value);

            if(xmlElementName.equalsIgnoreCase("title")){
                if(parsingItem){
                	//Log.d(Constant.LOGTAG, "Found " + xmlElementName + " - value: [" + value + "]");
                    // Strip html/xml entities
                    value = StringEscapeUtils.unescapeXml(value);

                    // Remove the 'eBay - ' or 'Amazon - ' from the titles
                    value = value.replaceFirst("[Aa][Mm][Aa][Zz][Oo][Nn] - ", "");
                    value = value.replaceFirst("[Ee][Bb][Aa][Yy] - ", "");
                	item.setTitle(value);
                }
                else if(parsingChannel){
                	//Log.d(Constant.LOGTAG, "Found (channel) " + xmlElementName + " - value: [" + value + "]");
                	channel.setTitle(value);
                }
            }
            else if(xmlElementName.equalsIgnoreCase("description")){
                if(parsingItem) {
                    // Regex's for Bens Bargains Feed
                    Pattern imagePattern = Pattern.compile("<img [^>]*src=\"(.*?[^\\\\])\"[^>]*/>");
                    Matcher imageMatcher = imagePattern.matcher(value);

                    Pattern descPattern = Pattern.compile("<img[^>]*>.*has(.*)for");
                    Matcher descMatcher = descPattern.matcher(value);

                    Pattern pricePattern = Pattern.compile("for <[Bb]>([^<]+)</[Bb]>");
                    Matcher priceMatcher = pricePattern.matcher(value);

                    if (imageMatcher.find()) {
                        String imageURI = imageMatcher.group(1);
                        item.setImageLink(imageURI);
                        //Log.d(Constant.LOGTAG, "Image: " + i);
                    }

                    if (descMatcher.find()) {
                        String desc = descMatcher.group(1);
                        item.setDescription(desc);
                        if (Constant.debug) Log.d(Constant.LOGTAG, "Desc: " + descMatcher.group(1));
                    }

                    if (priceMatcher.find()) {
                        String price = priceMatcher.group(1);
                        price = price.replaceAll("[^\\d\\.]", "");
                        item.setPrice(Float.parseFloat(price));
                        if (Constant.debug) Log.d(Constant.LOGTAG, "Price: " + price);
                    }
                }
            }
            else if(xmlElementName.equalsIgnoreCase("link")){
                if(parsingItem){
                	//Log.d(Constant.LOGTAG, "Found " + xmlElementName + " - value: [" + value + "]");
                	item.setLink(value);
                }
            }
            else if(xmlElementName.equalsIgnoreCase("pubDate")){
                if(parsingItem){
                	//Log.d(TAG, "Found " + xmlElementName);
                	item.setDate(value);
                }
            }
            else if(xmlElementName.equalsIgnoreCase("lastBuildDate")){
                if(parsingChannel){
                	//Log.d(TAG, "Found " + xmlElementName);
                	channel.setLastupdated(value);
                }
            }
            //else if(xmlElementName.equalsIgnoreCase("category")){
            //    if(parsingItem){
            //    	//Log.d(TAG, "Found " + xmlElementName);
            //    	item.setCategory(value);
            //    }
            //}
            else if(xmlElementName.equalsIgnoreCase("imageLink")){
                if(parsingItem){
                    if (Constant.debug) Log.d(Constant.LOGTAG, "(e) Found " + xmlElementName + " with value " + value);
                	item.setImageLink(value);
                }
            } else if(xmlElementName.equalsIgnoreCase("hot")){
                //if (Constant.debug) Log.d(Constant.LOGTAG, "(e) Skipping " + xmlElementName + " with value " + value);
            } else if(xmlElementName.equalsIgnoreCase("vendorName")){
                //if (Constant.debug) Log.d(Constant.LOGTAG, "(e) Skipping " + xmlElementName + " with value " + value);
            } else if(xmlElementName.equalsIgnoreCase("category")){
                //if (Constant.debug) Log.d(Constant.LOGTAG, "(e) Skipping " + xmlElementName + " with value " + value);
            } else if(xmlElementName.equalsIgnoreCase("author")){
                //if (Constant.debug) Log.d(Constant.LOGTAG, "(e) Skipping " + xmlElementName + " with value " + value);
            } else if(xmlElementName.equalsIgnoreCase("guid")){
                //if (Constant.debug) Log.d(Constant.LOGTAG, "(e) Skipping " + xmlElementName + " with value " + value);
            } else if(xmlElementName.equalsIgnoreCase("origLink")){
                //if (Constant.debug) Log.d(Constant.LOGTAG, "(e) Skipping " + xmlElementName + " with value " + value);
                if(parsingItem){
                    //Log.d(TAG, "Found " + xmlElementName);
                    item.setLink(value);
                }
            } else {
                if (Constant.debug) Log.d(Constant.LOGTAG, "TODO Add RssFeedHandler For: " + xmlElementName);
            }
        }

        @Override
        public void startDocument() throws SAXException {
            if (Constant.debug) Log.d(Constant.LOGTAG, "startDocument()");
            super.startDocument();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                super.startElement(uri, localName, qName, attributes);
                xmlElementName = qName;
                //Log.d("RSS_PARSER", "startElement: xmlElementName " + xmlElementName + " uri " + uri);

                if(xmlElementName.equalsIgnoreCase("channel")){
                    if (Constant.debug) Log.d(Constant.LOGTAG, "Parsing Channel");
                    parsingChannel = true;
                }
                else if(xmlElementName.equalsIgnoreCase("item")){
                	parsingItem = true;
                    if (Constant.debug) Log.d(Constant.LOGTAG, "Creating New Product");
                	item = new Product();
                }
                else if(xmlElementName.equalsIgnoreCase("atom:link") || xmlElementName.equalsIgnoreCase("atom10:link")){
                	channel.setLink(attributes.getValue("href"));
                }
                //else if(xmlElementName.equalsIgnoreCase("imageLink")){
                //    if(parsingItem){
                //    	
                //    }
                //}
                
                builder = new StringBuilder();
        }
}
