package com.code.attendance_project;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 2;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    // Updated UUIDs
    private static final UUID SERVICE_UUID = UUID.fromString("00002A05-0000-1000-8000-00805F9B34FB");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000FF01-0000-1000-8000-00805F9B34FB");

    private FirebaseAuth auth;
    private Button logoutButton;
    private TextView userDetailsTextView;
    private FirebaseUser user;

    private EditText divisionEditText;
    private EditText rollNoEditText;
    private Button submitButton;

    private final ActivityResultLauncher<Intent> enableBtLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Bluetooth has been enabled
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        logoutButton = findViewById(R.id.logout);
        userDetailsTextView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();

        // Check if user is logged in
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), login.class));
            finish();
        } else {
            userDetailsTextView.setText(user.getEmail());
        }

        // Logout button click listener
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getApplicationContext(), login.class));
            finish();
        });

        // Check permissions
        checkPermissions();

        // Initialize Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtLauncher.launch(enableBtIntent);
        }

        // Initialize UI elements
        divisionEditText = findViewById(R.id.division);
        rollNoEditText = findViewById(R.id.roll_no);
        submitButton = findViewById(R.id.Submit);

        // Submit button click listener
        submitButton.setOnClickListener(v -> {
            String division = divisionEditText.getText().toString();
            String rollNo = rollNoEditText.getText().toString();
            String registrationId = rollNo + "," + division;
            sendViaBluetooth(registrationId);
        });
    }

    private void checkPermissions() {
        // Check and request location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_LOCATION_PERMISSION);
        }

        // Check for Bluetooth permissions for API 31 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                }, REQUEST_LOCATION_PERMISSION);
            }
        }
    }

    private void sendViaBluetooth(String data) {
        // Check Bluetooth permissions again before proceeding
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // Handle missing permissions (e.g., show a message)
                return;
            }
        }

        try {
            String deviceAddress = "64:34:3A:38:61:00"; // Update with actual MAC address
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        bluetoothGatt.discoverServices();
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
                        if (service != null) {
                            BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                            if (characteristic != null) {
                                characteristic.setValue(data);
                                // Check permission before writing characteristic
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                                        bluetoothGatt.writeCharacteristic(characteristic);
                                    }
                                } else {
                                    bluetoothGatt.writeCharacteristic(characteristic);
                                }
                            }
                        }
                    }
                }
            });
        } catch (SecurityException e) {
            // Handle the exception (e.g., notify the user about missing permissions)
            e.printStackTrace(); // Optionally log the error
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0) {
                boolean allGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
                if (!allGranted) {
                    // Permission denied, handle accordingly (e.g., show a message)
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
