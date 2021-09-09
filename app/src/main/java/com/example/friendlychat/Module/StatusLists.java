package com.example.friendlychat.Module;

import java.util.List;

public class StatusLists {
    private List<List<Status>> statusLists;

    public StatusLists() {
    }

    public StatusLists(List<List<Status>> statusLists) {
        this.statusLists = statusLists;
    }

    public List<List<Status>> getStatusLists() {
        return statusLists;
    }
}
