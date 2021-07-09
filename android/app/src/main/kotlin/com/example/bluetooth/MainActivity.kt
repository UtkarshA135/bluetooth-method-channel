package com.example.bluetooth
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.IOException
import java.util.UUID;
import com.example.bluetooth.BluetoothServerController
val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
var devices = ArrayList<String?>()
val repositories = ArrayList<String?>()
var devicesMap = HashMap<String, BluetoothDevice>()
var mArrayAdapter: ArrayAdapter<String>? = null
class MainActivity: FlutterActivity() {
      private val CHANNEL = "username";
      private val MAKE_PAIR = "makepair";
      private val DEVICES = "devices";
      private val PAIRED = "paired";
      private val MAKE_UNPAIR = "makeunpair";
      private val MAKE_CONNECT = "makedeviceconnect";
     
      
fun check():Boolean 
    {
     if (bluetoothAdapter.isEnabled == false) {
        val REQUEST_ENABLE_BT=0;
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        return true;
      }
      else{
      bluetoothAdapter.disable();

       // onCreate(MainActivity)
       Log.d("Tag","isEnabled");
       return false
      
      }
    }
    
   
   // Create a BroadcastReceiver for ACTION_FOUND.



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("tag", "here");

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
        when (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            else -> requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)}
      //  BluetoothServerController(this).start()

    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            Log.d("action","here");
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address 
                    // MAC address
                    //BluetoothClient(device).start();
                    devices.add( deviceHardwareAddress);                    
                }
                

                
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()


        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }
   

   
  override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
      super.configureFlutterEngine(flutterEngine)
      MethodChannel(
          flutterEngine.dartExecutor.binaryMessenger,
          CHANNEL
      ).setMethodCallHandler { call, result ->
          if (call.method.equals("getUsername")) {
              check();
              if(bluetoothAdapter.isEnabled)
              result.success("Not Connected");
              else
              result.success("Connected");       
             

          }
      }
   
      MethodChannel(
          flutterEngine.dartExecutor.binaryMessenger,
          DEVICES
      ).setMethodCallHandler { call, result ->
          if (call.method.equals("getDevices")) {
           
              bluetoothAdapter.startDiscovery();
              if (bluetoothAdapter.isDiscovering()) {
               
                Log.d("TAG", "true")
               // BluetoothServerController(this).run()
                bluetoothAdapter.cancelDiscovery();
            } 

            else{
            for(device in devices){
              
                Log.d("devices",device.toString());
            }
        }
          
       
              
              result.success(devices);

          }
      }
      MethodChannel(flutterEngine.dartExecutor.binaryMessenger, PAIRED).setMethodCallHandler {
        call, result ->
            if(call.method.equals("getPaired")){
          
              val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
              pairedDevices?.forEach { device ->
                 val deviceName = device.name
                 val deviceHardwareAddress = device.address
                 repositories.add(deviceHardwareAddress);// MAC address
              }

              result.success(repositories);

            }
      }
      MethodChannel(flutterEngine.dartExecutor.binaryMessenger, MAKE_PAIR).setMethodCallHandler {
        call, result ->
            if(call.method.equals("makeDevicePair")){
              
                makeBond( bluetoothAdapter.getRemoteDevice("${call.argument("text") as String?}"));
               
 

             result.success(devices);
            }
      }
      MethodChannel(flutterEngine.dartExecutor.binaryMessenger, MAKE_UNPAIR).setMethodCallHandler {
        call, result ->
            if(call.method.equals("makeDeviceUnpair")){
              
                removeBond( bluetoothAdapter.getRemoteDevice("${call.argument("text") as String?}"));
        
             result.success(repositories);
            }
      }
      MethodChannel(flutterEngine.dartExecutor.binaryMessenger, MAKE_CONNECT).setMethodCallHandler {
        call, result ->
            if(call.method.equals("connect")){
              
                ConnectThread( bluetoothAdapter.getRemoteDevice("${call.argument("text") as String?}")).start();
        
             result.success(repositories);
            }
      }
      // Note: this method is invoked on the main thread.
  } // TODO
  fun makeBond(device: BluetoothDevice?) {
    try {
    device!!::class.java.getMethod("createBond").invoke(device)
    } catch (e: Exception) {
        Log.e("tag", "Adding bond has been failed. ${e.message}")
    }
}
fun removeBond(device: BluetoothDevice?) {
    try {
    device!!::class.java.getMethod("removeBond").invoke(device)
    } catch (e: Exception) {
        Log.e("tag", "Removing bond has been failed. ${e.message}")
    }
}
    }


    class BluetoothServerController(activity: MainActivity) : Thread() {
      private var cancelled: Boolean
      private val serverSocket: BluetoothServerSocket?
      private val activity = activity
  
      init {
          val btAdapter = BluetoothAdapter.getDefaultAdapter()
          if (btAdapter != null) {
              this.serverSocket = btAdapter.listenUsingRfcommWithServiceRecord("test", m_myUUID)
              this.cancelled = false
          } else {
              this.serverSocket = null
              this.cancelled = true
          }
  
      }
  
      override fun run() {
          var socket: BluetoothSocket
  
          while(true) {
              if (this.cancelled) {
                  break
              }
  
              try {
                  socket = serverSocket!!.accept()
              } catch(e: IOException) {
                  break
              }
  
              if (!this.cancelled && socket != null) {
                  Log.i("server", "Connecting")
                  BluetoothServer(this.activity, socket).start()
              }
          }
      }
  
      fun cancel() {
          this.cancelled = true
          this.serverSocket!!.close()
      }
  }
  
  class BluetoothServer(private val activity: MainActivity, private val socket: BluetoothSocket): Thread() {
      private val inputStream = this.socket.inputStream
      private val outputStream = this.socket.outputStream
  
      override fun run() {
          try {
              val available = inputStream.available()
              val bytes = ByteArray(available)
              Log.i("server", "Reading")
              inputStream.read(bytes, 0, available)
              val text = String(bytes)
              Log.i("server", "Message received")
              Log.i("server", text)
              Log.d("server",text);
             // activity.appendText(text)
          } catch (e: Exception) {
              Log.e("client", "Cannot read data", e)
          } finally {
              inputStream.close()
              outputStream.close()
              socket.close()
          }
      }
  }
  private  class ConnectThread(device: BluetoothDevice) : Thread() {

    private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(m_myUUID)
        
    }
 
    public override fun run() {
        var clazz = mmSocket?.remoteDevice?.javaClass
                    var paramTypes = arrayOf<Class<*>>(Integer.TYPE)
                    var m = clazz?.getMethod("createRfcommSocket", *paramTypes)
                    var fallbackSocket = m?.invoke(mmSocket?.remoteDevice, Integer.valueOf(1)) as BluetoothSocket
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter?.cancelDiscovery()
        try {
            fallbackSocket.connect()
           


        } catch (e: Exception) {
            e.printStackTrace()
          
        }
    }
 
    // Closes the client socket and causes the thread to finish.
    fun cancel() {
        try {
            mmSocket?.close()
        } catch (e: IOException) {
            Log.e("TAG", "Could not close the client socket", e)
        }
    }
 }