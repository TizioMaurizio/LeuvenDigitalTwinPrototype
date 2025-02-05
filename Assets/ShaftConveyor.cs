using UnityEngine;

public class ShaftConveyor : MonoBehaviour
{
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
    }

    //get father LargeMotor component and assign it to the currently colliding object with tag Conveyor
    void OnTriggerEnter(Collider other)
    {
        if (other.tag == "Conveyor")
        {
            LargeMotor largeMotor = GetComponentInParent<LargeMotor>();
            if (largeMotor != null)
            {
                Debug.Log("Assigning LargeMotor component " + largeMotor.name + " to conveyorBelt of " + other.name);
                largeMotor.conveyorBelt = other.GetComponent<ConveyorCollider>();
            }
        }
    }
}
