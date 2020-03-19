package com.em.btest;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        BluetoothService bluetoothService = new BluetoothService();
        bluetoothService.init();
        bluetoothService.startListening();
    }
}
