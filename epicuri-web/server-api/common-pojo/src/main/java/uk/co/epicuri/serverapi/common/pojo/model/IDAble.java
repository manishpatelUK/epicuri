package uk.co.epicuri.serverapi.common.pojo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtIgnoreField;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
public class IDAble {
    @Id
    private String id;

    @MgmtIgnoreField
    @Transient
    public transient static final String SEPARATOR = "-";

    public String getId() {
        return id;
    }

    public String extractParentId() {
        return extractParentId(id);
    }

    public static String extractParentId(String id) {
        if(id.contains(SEPARATOR)) {
            return id.substring(0, id.lastIndexOf(SEPARATOR));
        }

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static String generateId(IDAble parent, List<? extends IDAble> current) {
        return generateId(parent.getId(), current);
    }

    public static String generateId(String parentId, List<? extends IDAble> current) {
        return parentId + SEPARATOR + ControllerUtil.nextRandom(current.stream().map(IDAble::getId).collect(Collectors.toList()), 8);
    }

    public static String generateId(String parentId) {
        return parentId + SEPARATOR + ControllerUtil.nextRandom(8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IDAble idAble = (IDAble) o;

        return id != null ? id.equals(idAble.id) : idAble.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
