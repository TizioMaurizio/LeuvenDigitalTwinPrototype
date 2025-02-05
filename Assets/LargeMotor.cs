using UnityEngine;
using System.Collections.Generic;

public class LargeMotor : MonoBehaviour
{
    public float speed = 1f;
    public ConveyorCollider conveyorBelt;
    private Transform shaftTransform;
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        shaftTransform = transform.Find("Shaft");
    }

    // Update is called once per frame
    void FixedUpdate()
    {
        //rotate shaft on X axis
        //shaftTransform.Rotate(Vector3.forward, 10 * Time.deltaTime);
        // get conveyorBelt's collidingPallets, move them in direction of conveyorBelt's transform.forward with speed
        if (conveyorBelt == null)
        {
            return;
        }
        List<GameObject> pallets = conveyorBelt.collidingPallets;
        if (pallets != null)
        {
            foreach (GameObject pallet in pallets)
            {
                //move left of shaftTransform
                pallet.transform.position += shaftTransform.right * speed * Time.deltaTime;
            }
        }
    }
}
