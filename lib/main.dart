import 'dart:collection';

// ignore: import_of_legacy_library_into_null_safe
import 'package:bloc/bloc.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'package:flutter_bloc/flutter_bloc.dart';
void main() {
  runApp(MyApp());
}
List devices = [];
class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
      ),
      debugShowCheckedModeBanner: false,
      home: BlocProvider( 
        create:(context)=>DeviceBloc([]),
        child:
        MethodChannelImplementation()),
    );
  }
}
class MethodChannelImplementation extends StatefulWidget {
  const MethodChannelImplementation({ Key? key }) : super(key: key);

  @override
  _MethodChannelImplementationState createState() => _MethodChannelImplementationState();
}

class _MethodChannelImplementationState extends State<MethodChannelImplementation> {
  String methodChannelValue ="";
  // ignore: deprecated_member_use
  
  List paired=[];
  //MethodChannels
  static const methodChannel= MethodChannel("username");
  static const getDevicesChannel= MethodChannel("devices");
  static const getpairedDevices= MethodChannel("paired");
  static const makePairDevices= MethodChannel("makepair");
  static const makeUnpairDevices= MethodChannel("makeunpair");
  static const makeConnection= MethodChannel("makedeviceconnect");

 @override
  void initState() {
    // TODO: implement initState
    super.initState();
    methodChannelValue = "Not Connected";
    devices=[];
    paired=[];
    getList();
  }
  void getPaired() async{
    List<String>?  returnedPaired = await getpairedDevices.invokeListMethod<String>("getPaired");
     if(returnedPaired!=null)
   {
     setState(() {
      paired = returnedPaired.toSet().toList();
     
     });
   }

  }
  void initMethodChannel() async{
    String? returnedName =
   await methodChannel.invokeMethod<String>("getUsername");
   if(returnedName!=null)
   {
     setState(() {
       methodChannelValue=returnedName;
     });
   }

  }
    void getList() async{
      
    List? returneddevices =
   await getDevicesChannel.invokeMethod<List?>("getDevices");
   if(returneddevices!=null)
   {
     setState(() {
      devices = returneddevices.toSet().toList();
      for(final i in devices)
      {
        print("$i");
      }
     });
   }
   
  }
  void convert(String name)async{
    await makePairDevices.invokeMethod("makeDevicePair",{"text":name});
  }
    void unpair(String name)async{
    await makeUnpairDevices.invokeMethod("makeDeviceUnpair",{"text":name});
  }
   void connect(String name)async{
    await makeConnection.invokeMethod("connect",{"text":name});
  }
  @override
  Widget build(BuildContext context) {
    DeviceBloc bloc = BlocProvider.of<DeviceBloc>(context);
    return Scaffold(
      appBar: AppBar(centerTitle: true,
        title:Text("Method Channel Bluetooth")),
      body:
      Padding(padding: EdgeInsets.only(top:20),child:Center(
        child: Column(
        children: [
          methodChannelValue=="Not Connected" ? Icon(Icons.bluetooth_disabled):Icon(Icons.bluetooth_connected),
          Text(methodChannelValue),
          
          (
           
          
             TextButton(
                style: ButtonStyle(backgroundColor: MaterialStateProperty.all(Colors.blue[100])),
               onPressed: initMethodChannel, child: Text("Connect/Disconnect"),)),
             SizedBox(height:20),
        (
         
       
      
               TextButton(
                  style: ButtonStyle(backgroundColor: MaterialStateProperty.all(Colors.blue[100])),
                 onPressed:()=>{
               bloc.add(devices.length)
               } ,child:Text("Get Nearby devices"))),
        
          BlocBuilder<DeviceBloc,List<Device>>(
             builder: (context,devices) => Expanded(  
              child: ListView.builder(
            itemCount: devices.length,
                itemBuilder: (BuildContext context,int index){
            return ListTile(
           
              leading: Icon(Icons.bluetooth_connected),
              trailing: TextButton(
                 style: ButtonStyle(backgroundColor: MaterialStateProperty.all(Colors.blue[100])),
                onPressed:()=>{
                convert(devices[index].address)
              },child:Text("Pair")),
              title:Text(devices[index].address))
              ;
                  })),
          ),
        SizedBox(height:10),
        TextButton(
           style: ButtonStyle(backgroundColor: MaterialStateProperty.all(Colors.blue[100])),
          onPressed:getPaired ,child:Text("Get Paired devices")),
          paired!=null?  Expanded(
            
         
      
            child: ListView.builder(
          itemCount: paired.length,
              itemBuilder: (BuildContext context,int index){
          return ListTile(
            leading: IconButton(
              onPressed:()=>{
                connect(paired[index])
              },
              icon : Icon(Icons.bluetooth_connected),
          
            ),
             trailing:TextButton(
                style: ButtonStyle(backgroundColor: MaterialStateProperty.all(Colors.blue[100])),
               onPressed:()=>{
              unpair(paired[index])
            },child:Text("Unpair")),
            // SizedBox(width: 0.5,),
            // TextButton(onPressed:()=>{
            //   connect(paired[index])
            // },child:Text("Connect")),
             
            title:Text(paired[index])
            );
        })):Container()
        ],
      ))
        )    );
  }
}
//Bloc Implementattion
class Device{
  String address;
  Device({this.address=""});
}
class DeviceBloc extends Bloc<int,List<Device>>{
  DeviceBloc(List<Device> initialState) : super(initialState);

  @override
  // TODO: implement initialState
  List<Device> get initialState => [];

  @override
  Stream<List<Device>> mapEventToState(int event) async*{
    // TODO: implement mapEventToState
   List<Device> d = [];
   for(int i=0;i<event;i++)
   {
     d.add(Device(address: devices[i]));
   }
   yield d;
  }
}
