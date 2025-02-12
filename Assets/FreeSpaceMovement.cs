using UnityEngine;

public class FreeSpaceMovement : MonoBehaviour
{
    public float moveSpeed = 5f;
    public float fastMoveSpeed = 10f;
    public float mouseSensitivity = 100f;

    private float xRotation = 0f;

    void Update()
    {
        HandleMovement();
        HandleMouseLook();
    }

    void HandleMovement()
    {
        // Determine the current speed (normal or fast)
        float speed = Input.GetKey(KeyCode.LeftShift) ? fastMoveSpeed : moveSpeed;

        // Get input for movement
        float x = Input.GetAxis("Horizontal"); // A/D or Left/Right Arrow
        float z = Input.GetAxis("Vertical");   // W/S or Up/Down Arrow
        float y = 0f;

        // Up/down movement with Space and Control
        if (Input.GetKey(KeyCode.Space)) // Move up
        {
            y = 1f;
        }
        else if (Input.GetKey(KeyCode.LeftControl)) // Move down
        {
            y = -1f;
        }

        // Calculate movement direction relative to the camera's orientation
        Vector3 move = (transform.right * x) + (transform.up * y) + (transform.forward * z);
        transform.position += move * speed * Time.deltaTime;
    }

    void HandleMouseLook()
    {
        // Get mouse input
        float mouseX = Input.GetAxis("Mouse X") * mouseSensitivity * Time.deltaTime;
        float mouseY = Input.GetAxis("Mouse Y") * mouseSensitivity * Time.deltaTime;

        // Rotate the camera vertically
        xRotation -= mouseY;
        xRotation = Mathf.Clamp(xRotation, -90f, 90f); // Limit vertical rotation

        // Apply rotations
        transform.localRotation = Quaternion.Euler(xRotation, 0f, 0f); // Vertical (up/down)
        transform.parent.Rotate(Vector3.up * mouseX); // Horizontal (left/right)
    }
}