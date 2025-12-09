package com.mulberry.WebChat.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserListeningRoomService {
    private final ConcurrentHashMap<String, Set<String>> listeningRooms = new ConcurrentHashMap<String, Set<String>>();

    public void connectRoom(String username, String roomName) {
        listeningRooms.computeIfAbsent(username, k -> new HashSet<String>()).add(roomName);
    }

    public void disconnectRoom(String username, String roomName) {
        Set<String> rooms = listeningRooms.get(username);
        if (rooms == null || rooms.isEmpty()) {
            return;
        }

        rooms.remove(roomName);
        if (rooms.isEmpty()) {
            listeningRooms.remove(username);
        }
    }

    public boolean isListening(String username, String roomName) {
        return listeningRooms.getOrDefault(username, Collections.emptySet()).contains(roomName);
    }

    public Set<String> getListeners(String roomName) {
        Set<String> listeners = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : listeningRooms.entrySet()) {
            Set<String> rooms = entry.getValue();
            if (rooms != null && rooms.contains(roomName)) {
                listeners.add(entry.getKey());
            }
        }

        return listeners;
    }

    public void clearUser(String username) {
        listeningRooms.remove(username);
    }

    public void printCurrentUsers() {
        System.out.println("Current: " + listeningRooms);
    }
}
