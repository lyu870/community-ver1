// ReadNotificationsRequestDto.java
package com.hello.community.notification.dto;

import java.util.List;

public class ReadNotificationsRequestDto {

    private List<Long> ids;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
