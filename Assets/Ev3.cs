using UnityEngine;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using Newtonsoft.Json;

public class Ev3 : MonoBehaviour
{
    public int udpPort = 11002;
    private UdpClient udpClient;
    private Thread receiveThread;
    private bool isRunning = true;
    public GameObject outA;
    public GameObject outB;
    public GameObject outC;
    public GameObject outD;
    public GameObject in1;
    public GameObject in2;
    public GameObject in3;
    public GameObject in4;
    public bool button;
    // Thread-safe variables
    private float receivedSpeed = 0f;
    private bool hasNewSpeed = false;

    private Dictionary<string, GameObject> portsDict = new Dictionary<string, GameObject>();
    private Dictionary<string, float> portsValuesDict = new Dictionary<string, float>();
    private bool newValue = false;

    void Awake()
    {
        
        //for each gameobject in items of portsDict set material to material of this gameobject
        foreach (KeyValuePair<string, GameObject> entry in portsDict)
        {
            if (entry.Value != null)
            {
                entry.Value.GetComponent<Renderer>().material = gameObject.GetComponent<Renderer>().material;
            }
        }
    }
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        // Initialize UDP client
        udpClient = new UdpClient(udpPort);

        // Start a thread to receive messages
        receiveThread = new Thread(new ThreadStart(ReceiveMessages));
        receiveThread.IsBackground = true;
        receiveThread.Start();

        //create a dict out of gameobject variables
        portsDict.Add("outA", outA);
        portsDict.Add("outB", outB);
        portsDict.Add("outC", outC);
        portsDict.Add("outD", outD);
        portsDict.Add("in1", in1);
        portsDict.Add("in2", in2);
        portsDict.Add("in3", in3);
        portsDict.Add("in4", in4);
        portsValuesDict.Add("outA", 0);
        portsValuesDict.Add("outB", 0);
        portsValuesDict.Add("outC", 0);
        portsValuesDict.Add("outD", 0);
        portsValuesDict.Add("in1", 0);
        portsValuesDict.Add("in2", 0);
        portsValuesDict.Add("in3", 0);
        portsValuesDict.Add("in4", 0);

    }

    // Update is called once per frame
    void FixedUpdate()
    {
        if (newValue)
        {
            UpdatePorts();
            newValue = false;
        }
    }

    void UpdatePorts()
    {
        //outA.GetComponent<LargeMotor>().speed = portsValuesDict["outA"];
        foreach (KeyValuePair<string, GameObject> entry in portsDict)
        {
            if (entry.Value != null)
            {
                if (entry.Value.GetComponent<LargeMotor>() != null)
                {
                    entry.Value.GetComponent<LargeMotor>().speed = portsValuesDict[entry.Key];
                }
                else if (entry.Value.GetComponent<Pusher>() != null)
                {
                    entry.Value.GetComponent<Pusher>().push = portsValuesDict[entry.Key] == 1;
                }
                else if (entry.Value.GetComponent<Ev3Button>() != null)
                {
                    entry.Value.GetComponent<Ev3Button>().press = portsValuesDict[entry.Key] == 1;
                }
            }
        }
    }

    void HandleMessage(Dictionary<string, string> message)
    {
        //if get outA then set outA component LargeMotor speed to value
        //if (message.ContainsKey("outA"))
        //{
        //    float speed = float.Parse(message["outA"]);
        //    portsDict["outA"] = speed;
        //}
        //do this for all ports
        foreach (KeyValuePair<string, string> entry in message)
        {
            if (portsValuesDict.ContainsKey(entry.Key))
            {
                float speed = float.Parse(entry.Value);
                portsValuesDict[entry.Key] = speed;
                newValue = true;
            }
        }
    }
    void ReceiveMessages()
    {
        IPEndPoint remoteEndPoint = new IPEndPoint(IPAddress.Any, udpPort);

        while (isRunning)
        {
            try
            {
                // Receive data from the network
                byte[] data = udpClient.Receive(ref remoteEndPoint);
                string message = Encoding.UTF8.GetString(data);
                Debug.Log("Received UDP message from: " + remoteEndPoint.Address + ":" + remoteEndPoint.Port + " " + message);
                // Deserialize JSON into a Dictionary<string, string>
                var jsonDictionary = JsonConvert.DeserializeObject<Dictionary<string, string>>(message);
                HandleMessage(jsonDictionary);
                //float speed = float.Parse(jsonDictionary["outA"]);
                //Debug.Log(speed);
            }
            catch (System.Exception e)
            {
                Debug.LogError($"Error receiving UDP message: {e.Message}");
            }
        }
    }

    void OnDestroy()
    {
        // Stop the receive thread and close the UDP client
        isRunning = false;
        udpClient.Close();
    }
}
