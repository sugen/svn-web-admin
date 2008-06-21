/**
 *
 */
package com.wkinney.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author wkinney
 *
 */
public class Membership  implements IsSerializable {


    private String group;
    private String member;

    public Membership() {

    }

    public Membership(String group, String member) {
        super();
        this.group = group;
        this.member = member;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((member == null) ? 0 : member.hashCode());
        return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Membership))
            return false;
        final Membership other = (Membership) obj;
        if (group == null) {
            if (other.group != null)
                return false;
        } else if (!group.equals(other.group))
            return false;
        if (member == null) {
            if (other.member != null)
                return false;
        } else if (!member.equals(other.member))
            return false;
        return true;
    }
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }
    public String getMember() {
        return member;
    }
    public void setMember(String member) {
        this.member = member;
    }



}
