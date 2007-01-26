package com.jbidwatcher;
/*
 * Copyright (c) 2000-2007, CyberFOX Software, Inc. All Rights Reserved.
 *
 * Developed by mrs (Morgan Schweers)
 */

import com.jbidwatcher.config.JConfig;
import com.jbidwatcher.xml.XMLElement;
import com.jbidwatcher.xml.XMLSerialize;

import java.util.*;
import java.io.File;

/*!@class DeletedManager
 * @brief Manage deleted items, so they are not added again.
 */
public class DeletedManager implements XMLSerialize {
  private Map<String, Date> _deletedItems = null;

  public DeletedManager() {
    _deletedItems = new TreeMap<String, Date>();
  }

  public boolean isDeleted(String id) {
    if(JConfig.queryConfiguration("deleted.ignore", "true").equals("true")) {
      return _deletedItems.containsKey(id);
    }
    undelete(id);
    return false;
  }

  public void undelete(String id) {
    _deletedItems.remove(id);
  }

  private void killFiles(String id) {
    String outPath = JConfig.queryConfiguration("auctions.savepath");
    String imgPath = outPath + System.getProperty("file.separator") + id;

    File thumb = new File(imgPath+".jpg");
    if(thumb.exists()) thumb.delete();

    File realThumb = new File(imgPath + "_t.jpg");
    if (realThumb.exists()) realThumb.delete();

    File badBlocker = new File(imgPath + "_b.jpg");
    if (badBlocker.exists()) badBlocker.delete();

    File html = new File(imgPath+".html.gz");
    if(html.exists()) html.delete();

    File htmlBackup = new File(imgPath+".html.gz~");
    if(htmlBackup.exists()) htmlBackup.delete();
  }

  public void delete(String id) {
    if(!isDeleted(id)) {
      killFiles(id);
      _deletedItems.put(id, new Date());
    }
  }

  public void fromXML(XMLElement deletedEntries) {
    //  A lack of deleted entries is not a failure.
    if(deletedEntries == null) return;

    //  Walk over all the deleted entries and store them.
    Iterator<XMLElement> delWalk = deletedEntries.getChildren();
    while(delWalk.hasNext()) {
      XMLElement delEntry = delWalk.next();
      if(delEntry.getTagName().equals("delentry")) {
        String strDel = delEntry.getProperty("when");
        Date whenDeleted;
        if(strDel == null) {
          whenDeleted = new Date();
        } else {
          whenDeleted = new Date(Long.parseLong(strDel));
        }
        _deletedItems.put(delEntry.getContents(), whenDeleted);
      }
    }
  }

  public XMLElement toXML() {
    XMLElement xmlResult = new XMLElement("deleted");
    for (String delId : _deletedItems.keySet()) {
      XMLElement entry = new XMLElement("delentry");
      entry.setProperty("when", Long.toString((_deletedItems.get(delId)).getTime()));
      entry.setContents(delId);
      xmlResult.addChild(entry);
    }

    return xmlResult;
  }
}
