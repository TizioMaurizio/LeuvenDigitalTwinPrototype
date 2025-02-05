using UnityEngine;

public class Ev3Button : MonoBehaviour
{
    public bool press;
    public GameObject ev3Light;
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        //if press set ev3light to green
        if (press)
        {
            ev3Light.GetComponent<Renderer>().material.color = Color.green;
            press = false;
        }
    }
}
