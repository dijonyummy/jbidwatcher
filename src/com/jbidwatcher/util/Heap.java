package com.jbidwatcher.util;
/*
 * Copyright (c) 2000-2007, CyberFOX Software, Inc. All Rights Reserved.
 *
 * Developed by mrs (Morgan Schweers)
 */

/*
  File: Heap.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  29Aug1998  dl               Refactored from BoundedPriorityQueue
*/

import com.jbidwatcher.queue.TimeQueue;

import java.util.*;

/**
 * A heap-based priority queue, without any concurrency control
 * (i.e., no blocking on empty/full states).
 * This class provides the data structure mechanics for BoundedPriorityQueue.
 * <p>
 * The class currently uses a standard array-based heap, as described
 * in, for example, Sedgewick's Algorithms text. All methods
 * are fully synchronized. In the future,
 * it may instead use structures permitting finer-grained locking.
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 **/

@SuppressWarnings({"JavaDoc"})
public class Heap  {
  protected Object[] nodes_;  // the tree nodes, packed into an array
  protected int count_ = 0;   // number of used slots
  protected final Comparator cmp_;  // for ordering

  /**
   * Create a Heap with the given initial capacity and comparator
   *
   * @param capacity
   * @param cmp
   *
   * @exception IllegalArgumentException if capacity less or equal to zero
   */
  public Heap(int capacity, Comparator cmp)
   throws IllegalArgumentException {
    if (capacity <= 0) throw new IllegalArgumentException();
    nodes_ = new Object[capacity];
    cmp_ = cmp;
  }

  /**
   * Create a Heap with the given capacity,
   * and relying on natural ordering.
   *
   * @param capacity
   */
  public Heap(int capacity) {
    this(capacity, null);
  }


/**
  * perform element comaprisons using comparator or natural ordering
  *
  * @param a
  * @param b
  *
  * @return
  */
  protected int compare(Object a, Object b) {
    if (cmp_ == null)
      return ((Comparable)a).compareTo(b);
    else
      return cmp_.compare(a, b);
  }

  // indexes of heap parents and children
  protected final int parent(int k) { return (k - 1) / 2;  }
  protected final int left(int k)   { return 2 * k + 1; }
  protected final int right(int k)  { return 2 * (k + 1); }

  /**
   * insert an element, resize if necessary
   *
   * @param x
   */
  public synchronized void insert(Object x) {
    if (count_ >= nodes_.length) {
      int newcap =  3 * nodes_.length / 2 + 1;
      Object[] newnodes = new Object[newcap];
      System.arraycopy(nodes_, 0, newnodes, 0, nodes_.length);
      nodes_ = newnodes;
    }

    int k = count_;
    ++count_;
    while (k > 0) {
      int par = parent(k);
      if (compare(x, nodes_[par]) < 0) {
        nodes_[k] = nodes_[par];
        k = par;
      }
      else break;
    }
    nodes_[k] = x;
  }

  /**
   * Return and remove least element, or null if empty
   *
   * @return
   */
  public synchronized Object extract() {
    if (count_ < 1) return null;

    int k = 0; // take element at root;
    return extractElementAt(k);
  }

  private Object extractElementAt(int k) {
    Object least = nodes_[k];
    --count_;
    Object x = nodes_[count_];
    for (;;) {
      int l = left(k);
      if (l >= count_)
        break;
      else {
        int r = right(k);
        int child = (r >= count_ || compare(nodes_[l], nodes_[r]) < 0)? l : r;
        if (compare(x, nodes_[child]) > 0) {
          nodes_[k] = nodes_[child];
          k = child;
        }
        else break;
      }
    }
    nodes_[k] = x;
    //  Prevent leakage...?
    nodes_[count_] = null;
    return least;
  }

  /** Return least element without removing it, or null if empty **/
  public synchronized Object peek() {
    if (count_ > 0)
      return nodes_[0];
    else
      return null;
  }

  /** Return number of elements **/
  public synchronized int size() {
    return count_;
  }

  /** remove all elements **/
  public synchronized void clear() {
    //  Clear out the array, to avoid keeping references around.
    for(int i=0; i<count_; i++) nodes_[i] = null;
    count_ = 0;
  }

  public List getUnsorted() {
    List rval = Arrays.asList(nodes_);
    return rval.subList(0, count_);
  }

  public List getSorted() {
    List base = getUnsorted();
    Collections.sort(base);
    return base;
  }

  public boolean erase(TimeQueue.QObject o) {
    for (int i = 0; i < nodes_.length; i++) {
      if(nodes_[i] == o) {
        extractElementAt(i);
        return true;
      }
    }

    return false;
  }
}
