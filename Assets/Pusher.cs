using UnityEngine;

public class Pusher : MonoBehaviour
{
    public Head head;
    public bool push;
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        if (push)
        {
            head.state = Head.State.EXTENDING;
            push = false;
        }
    }
}
