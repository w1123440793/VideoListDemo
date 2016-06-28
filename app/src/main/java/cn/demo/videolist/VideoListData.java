package cn.demo.videolist;

import java.io.Serializable;
import java.util.List;

/**
 * Author  wangchenchen
 * Description
 */
public class VideoListData implements Serializable {

    private List<VideoItemData> list;

    public List<VideoItemData> getList() {
        return list;
    }

    public void setList(List<VideoItemData> list) {
        this.list = list;
    }
}
