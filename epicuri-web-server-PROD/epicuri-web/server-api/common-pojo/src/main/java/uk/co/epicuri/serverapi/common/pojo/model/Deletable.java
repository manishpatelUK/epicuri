package uk.co.epicuri.serverapi.common.pojo.model;

public class Deletable extends IDAble{
    private Long deleted;

    public Long getDeleted() {
        return deleted;
    }

    public void setDeleted(Long deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Deletable deletable = (Deletable) o;

        return deleted != null ? deleted.equals(deletable.deleted) : deletable.deleted == null;

    }

    @Override
    public int hashCode() {
        return deleted != null ? deleted.hashCode() : 0;
    }
}
