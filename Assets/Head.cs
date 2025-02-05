using UnityEngine;

public class Head : MonoBehaviour
{
    public GameObject retractLimit;
    public GameObject extendLimit;
    public bool disableLimitMeshes;
    // enum state: EXTENDING, RETRACTING, STOPPED
    public enum State
    {
        STOPPED,
        EXTENDING,
        RETRACTING
    }
    public State state = State.STOPPED;
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        if (disableLimitMeshes)
        {
            //disable meshrenderers of retractLimit and extendLimit
            retractLimit.GetComponent<MeshRenderer>().enabled = false;
            extendLimit.GetComponent<MeshRenderer>().enabled = false;
        }
        
    }

    void OnTriggerEnter(Collider other)
    {
        if (state == State.EXTENDING && other.gameObject == extendLimit)
        {
            Debug.Log("Extend Limit Reached");
            state = State.RETRACTING;
        }
        else if (state == State.RETRACTING && other.gameObject == retractLimit)
        {
            Debug.Log("Retract Limit Reached");
            state = State.STOPPED;
        }
    }
    // Update is called once per frame
    void FixedUpdate()
    {
        //move forward or backward depending on state
        if (state == State.EXTENDING)
        {
            transform.Translate(Vector3.right * Time.deltaTime);
        }
        else if (state == State.RETRACTING)
        {
            transform.Translate(Vector3.right * -Time.deltaTime);
        }
    }
}
