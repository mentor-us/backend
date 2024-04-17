package com.hcmus.mentor.backend.service.query.filter;


import java.util.Objects;

public class RangeFilter<FIELD_TYPE extends Comparable<? super FIELD_TYPE>> extends Filter<FIELD_TYPE> {
    private static final long serialVersionUID = 1L;
    private FIELD_TYPE greaterThan;
    private FIELD_TYPE lessThan;
    private FIELD_TYPE greaterThanOrEqual;
    private FIELD_TYPE lessThanOrEqual;

    public RangeFilter() {
    }

    public RangeFilter(RangeFilter<FIELD_TYPE> filter) {
        super(filter);
        this.greaterThan = filter.greaterThan;
        this.lessThan = filter.lessThan;
        this.greaterThanOrEqual = filter.greaterThanOrEqual;
        this.lessThanOrEqual = filter.lessThanOrEqual;
    }

    public RangeFilter<FIELD_TYPE> copy() {
        return new RangeFilter(this);
    }

    public FIELD_TYPE getGreaterThan() {
        return this.greaterThan;
    }

    public RangeFilter<FIELD_TYPE> setGreaterThan(FIELD_TYPE greaterThan) {
        this.greaterThan = greaterThan;
        return this;
    }

    public FIELD_TYPE getLessThan() {
        return this.lessThan;
    }

    public RangeFilter<FIELD_TYPE> setLessThan(FIELD_TYPE lessThan) {
        this.lessThan = lessThan;
        return this;
    }

    public FIELD_TYPE getGreaterThanOrEqual() {
        return this.greaterThanOrEqual;
    }

    public RangeFilter<FIELD_TYPE> setGreaterThanOrEqual(FIELD_TYPE greaterThanOrEqual) {
        this.greaterThanOrEqual = greaterThanOrEqual;
        return this;
    }

    public FIELD_TYPE getLessThanOrEqual() {
        return this.lessThanOrEqual;
    }

    public RangeFilter<FIELD_TYPE> setLessThanOrEqual(FIELD_TYPE lessThanOrEqual) {
        this.lessThanOrEqual = lessThanOrEqual;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            if (!super.equals(o)) {
                return false;
            } else {
                RangeFilter<?> that = (RangeFilter)o;
                return Objects.equals(this.greaterThan, that.greaterThan) && Objects.equals(this.lessThan, that.lessThan) && Objects.equals(this.greaterThanOrEqual, that.greaterThanOrEqual) && Objects.equals(this.lessThanOrEqual, that.lessThanOrEqual);
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(super.hashCode(), this.greaterThan, this.lessThan, this.greaterThanOrEqual, this.lessThanOrEqual);
    }

    public String toString() {
        String var10000 = this.getFilterName();
        return var10000 + " [" + (this.getEquals() != null ? "equals=" + this.getEquals() + ", " : "") + (this.getNotEquals() != null ? "notEquals=" + this.getNotEquals() + ", " : "") + (this.getSpecified() != null ? "specified=" + this.getSpecified() + ", " : "") + (this.getIn() != null ? "in=" + this.getIn() + ", " : "") + (this.getNotIn() != null ? "notIn=" + this.getNotIn() + ", " : "") + (this.getGreaterThan() != null ? "greaterThan=" + this.getGreaterThan() + ", " : "") + (this.getLessThan() != null ? "lessThan=" + this.getLessThan() + ", " : "") + (this.getGreaterThanOrEqual() != null ? "greaterThanOrEqual=" + this.getGreaterThanOrEqual() + ", " : "") + (this.getLessThanOrEqual() != null ? "lessThanOrEqual=" + this.getLessThanOrEqual() : "") + "]";
    }
}
