package com.rimusdesign.webcrawler;


import com.rimusdesign.webcrawler.model.Page;
import com.rimusdesign.webcrawler.model.PageState;

import java.util.HashMap;
import java.util.HashSet;


/**
 * @author Rimas Krivickas.
 */
public class Repository {


    private HashMap<String, Page> items = new HashMap<>();


    public void save (Page page) {

        items.put(page.getId(), page);
    }


    public HashSet<Page> getItems () {

        return new HashSet<>(items.values());
    }


    public boolean contains (String id) {

        return items.keySet().contains(id);
    }


    public boolean hasPendingItems () {

        for (Page page : items.values()) {
            if (page.getState().equals(PageState.PENDING)) return true;
        }
        return false;
    }

}
