//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.10.27 at 07:46:38 PM CDT 
//


package BLAST;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "hsp"
})
@XmlRootElement(name = "Hit_hsps")
public class HitHsps {

    @XmlElement(name = "Hsp")
    protected List<Hsp> hsp;

    /**
     * Gets the value of the hsp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hsp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHsp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Hsp }
     * 
     * 
     */
    public List<Hsp> getHsp() {
        if (hsp == null) {
            hsp = new ArrayList<Hsp>();
        }
        return this.hsp;
    }

}
