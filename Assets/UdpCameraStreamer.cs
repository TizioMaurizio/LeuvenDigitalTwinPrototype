using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using System.Net.Sockets;

public class UdpCameraStreamer : MonoBehaviour
{
    //streams a rawimage via udp to target ip and port
    public RenderTexture sendTexture;
    public string targetIP = "127.0.0.1";
    public int targetPort = 25667;
    public UdpClient udpClient;
    private Texture2D tex;
    // Start is called before the first frame update
    void Start()
    {
        // get EV3 component in father of father and copy variable udpPort then check its variables in1, in2, in3, in4, and for the corresponding one add inn * 1000 to udpPort
        targetPort = GetComponentInParent<Transform>().GetComponentInParent<Ev3>().udpPort;
        GameObject in1 = GetComponentInParent<Transform>().GetComponentInParent<Ev3>().in1;
        //Debug.Log(in1);
        GameObject in2 = GetComponentInParent<Transform>().GetComponentInParent<Ev3>().in2;
        //Debug.Log(in2);
        GameObject in3 = GetComponentInParent<Transform>().GetComponentInParent<Ev3>().in3;
        //Debug.Log(in3);
        GameObject in4 = GetComponentInParent<Transform>().GetComponentInParent<Ev3>().in4;
        //Debug.Log(in4);
        //Debug.Log("This gameobject is " + this.gameObject);
        //check if in1 is this gameobject's parent
        GameObject parent = this.gameObject.transform.parent.gameObject;
        if (in1 == parent)
        {
            targetPort += 1000;
        }
        //check if in2 is this gameobject's parent
        if (in2 == parent)
        {
            targetPort += 2000;
        }
        //check if in3 is this gameobject's parent
        if (in3 == parent)
        {
            targetPort += 3000;
        }
        //check if in4 is this gameobject's parent
        if (in4 == parent)
        {
            targetPort += 4000;
        }
        //set up udp socket to send
        udpClient = new UdpClient();
        udpClient.Connect(targetIP, targetPort);
        //socket size
        udpClient.Client.SendBufferSize = 65536;

        sendTexture = new RenderTexture(640, 480, 24);
        // set camera of this gameobject to render to rendertexture
        Camera cam = GetComponent<Camera>();
        cam.targetTexture = sendTexture;
        tex = new Texture2D(sendTexture.width, sendTexture.height, TextureFormat.RGB24, false);

    }

    //prevtime
    float _prevSendTime = 0;
    // Update is called once per frame
    void Update()
    {
        //send only if 60ms have passed
        if (Time.time - _prevSendTime > 0.06f)
        {
            _prevSendTime = Time.time;
            SendTexture();
        }
    }

    void SendTexture(){
        if (sendTexture != null)
        {
            //send to ip
            RenderTexture.active = sendTexture;
            tex.ReadPixels(new Rect(0, 0, sendTexture.width, sendTexture.height), 0, 0);
            tex.Apply();
            //jpg compress
            byte[] jpg = tex.EncodeToJPG(50);
            udpClient.Send(jpg, jpg.Length);
        }
    }
}
