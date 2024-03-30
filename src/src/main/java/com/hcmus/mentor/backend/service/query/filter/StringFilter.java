package com.hcmus.mentor.backend.service.query.filter;


import java.util.Objects;

public class StringFilter extends Filter<String> {
    private static final long serialVersionUID = 1L;
    private String contains;
    private String doesNotContain;

    public StringFilter() {
    }

    public StringFilter(StringFilter filter) {
        super(filter);
        this.contains = filter.contains;
        this.doesNotContain = filter.doesNotContain;
    }

    public StringFilter copy() {
        return new StringFilter(this);
    }

    public String getContains() {
        return this.contains;
    }

    public StringFilter setContains(String contains) {
        this.contains = contains;
        return this;
    }

    public String getDoesNotContain() {
        return this.doesNotContain;
    }

    public StringFilter setDoesNotContain(String doesNotContain) {
        this.doesNotContain = doesNotContain;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            if (!super.equals(o)) {
                return false;
            } else {
                StringFilter that = (StringFilter)o;
                return Objects.equals(this.contains, that.contains) && Objects.equals(this.doesNotContain, that.doesNotContain);
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{super.hashCode(), this.contains, this.doesNotContain});
    }

    public String toString() {
        String var10000 = this.getFilterName();
        return var10000 + " [" + (this.getEquals() != null ? "equals=" + (String)this.getEquals() + ", " : "") + (this.getNotEquals() != null ? "notEquals=" + (String)this.getNotEquals() + ", " : "") + (this.getSpecified() != null ? "specified=" + this.getSpecified() + ", " : "") + (this.getIn() != null ? "in=" + String.valueOf(this.getIn()) + ", " : "") + (this.getNotIn() != null ? "notIn=" + String.valueOf(this.getNotIn()) + ", " : "") + (this.getContains() != null ? "contains=" + this.getContains() + ", " : "") + (this.getDoesNotContain() != null ? "doesNotContain=" + this.getDoesNotContain() : "") + "]";
    }
}
