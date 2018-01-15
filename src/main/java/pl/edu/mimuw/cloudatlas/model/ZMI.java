/**
 * Copyright (c) 2014, University of Warsaw
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package pl.edu.mimuw.cloudatlas.model;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * A zone management information. This object is a single node in a zone hierarchy. It stores zone attributes as well as
 * references to its father and sons in the tree.
 */
public class ZMI implements Cloneable, Serializable {
    private AttributesMap attributes = new AttributesMap();

    private List<ZMI> sons = new ArrayList<ZMI>();
    private ZMI father;

    private Long timestamp;

    /**
     * Creates a new ZMI with no father (the root zone) and empty sons list.
     */
    public ZMI() {
        this((ZMI) null);
    }

    public ZMI(Long tsMillis) {
        this((ZMI) null);
        this.timestamp = tsMillis;
    }

    /**
     * Creates a new ZMI with the specified node as a father and empty sons list. This method does not perform any
     * operation on the <code>father</code>. Especially, setting this object as a <code>father</code>'s son must be done
     * separately.
     *
     * @param father a father of this ZMI
     * @see #addSon(ZMI)
     */
    public ZMI(ZMI father) {
        this.father = father;
        this.timestamp = System.currentTimeMillis();
    }

    public ZMI(ZMI father, Long tsMillis) {
        this.father = father;
        this.timestamp = tsMillis;
    }

    /**
     * Gets a father of this ZMI in a tree.
     *
     * @return a father of this ZMI or <code>null</code> if this is the root zone
     */
    public ZMI getFather() {
        return father;
    }

    /**
     * Sets or changes a father of this ZMI in a tree. This method does not perform any operation on the
     * <code>father</code>. Especially, setting this object as a <code>father</code>'s son must be done separately.
     *
     * @param father a new father for this ZMI
     * @see #addSon(ZMI)
     */
    public void setFather(ZMI father) {
        this.father = father;
    }

    /**
     * Gets a list of sons of this ZMI in a tree. Modifying a return value will cause an exception.
     *
     * @return
     */
    public List<ZMI> getSons() {
        return Collections.unmodifiableList(sons);
    }

    /**
     * Adds the specified ZMI to the list of sons of this ZMI. This method does not perform any operation on a
     * <code>son</code>. Especially, setting this object as a <code>son</code>'s father must be done separately.
     *
     * @param son
     * @see #ZMI(ZMI)
     * @see #setFather(ZMI)
     */
    public void addSon(ZMI son) {
        sons.add(son);
    }

    /**
     * Removes the specified ZMI from the list of sons of this ZMI. This method does not perform any operation on a
     * <code>son</code>. Especially, its father remains unchanged.
     *
     * @param son
     * @see #setFather(ZMI)
     */
    public void removeSon(ZMI son) {
        sons.remove(son);
    }

    public void removeSons() {
        this.sons.clear();
    }

    /**
     * Gets a map of all the attributes stored in this ZMI.
     *
     * @return map of attributes
     */
    public AttributesMap getAttributes() {
        return attributes;
    }

    /**
     * Prints recursively in a prefix order (starting from this ZMI) a whole tree with all the attributes.
     *
     * @param stream a destination stream
     * @see #toString()
     */
    public void printAttributes(PrintStream stream) {
        for (Entry<Attribute, Value> entry : attributes)
            stream.println(entry.getKey() + " : " + entry.getValue().getType() + " = " + entry.getValue());
        System.out.println();
        for (ZMI son : sons)
            son.printAttributes(stream);
    }

    /**
     * Creates an independent copy of a whole hierarchy. A returned ZMI has the same reference as a father (but the
     * father does not have a reference to it as a son). For the root zone, the copy is completely independent, since
     * its father is <code>null</code>.
     *
     * @return a deep copy of this ZMI
     */
    @Override
    public ZMI clone() {
        ZMI result = new ZMI(father);
        result.attributes.add(attributes.clone());
        for (ZMI son : sons) {
            ZMI sonClone = son.clone();
            result.sons.add(sonClone);
            sonClone.father = result;
        }
        return result;
    }

    public ZMI cloneSingleNode() {
        ZMI result = new ZMI();
        result.attributes.add(attributes.clone());
        return result;
    }

    public void cloneAttributes(ZMI cloneFrom) {
        this.attributes = cloneFrom.attributes.clone();
    }

    /**
     * Prints a textual representation of this ZMI. It contains only attributes of this node.
     *
     * @return a textual representation of this object
     * @see #printAttributes(PrintStream)
     */
    @Override
    public String toString() {
        return attributes.toString();
    }

    public Long getTimestamp() { return this.timestamp; }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public ArrayList<ValueContact> getContacts() {
        ArrayList<ValueContact> ret = new ArrayList<>();

        Value contactsAttr = this.attributes.getOrNull("contacts");
        if (contactsAttr != null) {
            Type expectedType = new TypeCollection(Type.PrimaryType.LIST, TypePrimitive.CONTACT);
            try {
                contactsAttr = contactsAttr.convertTo(expectedType);
            } catch (Exception e) {
                System.out.println("ZMI: Attribute contacts with invalid type detected");
                e.printStackTrace();
                return ret;
            }
            ValueList contactsList = (ValueList) contactsAttr;
            for (Value contact : contactsList) {
                ret.add((ValueContact) contact);
            }
        }

        return  ret;
    }
}
