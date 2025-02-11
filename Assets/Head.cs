using UnityEngine;

public class Head : MonoBehaviour
{
    public GameObject retractLimit;
    public GameObject extendLimit;
    public bool disableLimitMeshes;
    public int direction = 1;
    public float stopTime = 1.0f;
    private float waitedTime = 0.0f;
    private Vector3 directionVector;
    // enum state: EXTENDING, RETRACTING, STOPPED
    public enum State
    {
        STOPPED,
        EXTENDING,
        RETRACTING,
        WAITING
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
        if (direction > 0)
        {
            directionVector = Vector3.right;
        }
        else
        {
            directionVector = Vector3.left;
        }
        
    }

    void OnTriggerEnter(Collider other)
    {
        if (state == State.EXTENDING && other.gameObject == extendLimit)
        {
            Debug.Log("Extend Limit Reached");
            state = State.WAITING;
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
            transform.Translate(directionVector * Time.deltaTime);
        }
        else if (state == State.RETRACTING)
        {
            transform.Translate(directionVector * -Time.deltaTime);
        }

        if (state == State.WAITING)
        {
            //wait for time
            waitedTime += Time.deltaTime;
            if (waitedTime >= stopTime)
            {
                state = State.RETRACTING;
                waitedTime = 0.0f;
            }
        }
    }
}
