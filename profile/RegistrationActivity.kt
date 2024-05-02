@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is already onboarded
        if (sharedPrefManager.getIsOnboarded()) {
            // Navigate to the main activity or another suitable destination
            startActivity(Intent(this, StudentMainActivity::class.java))
            finish()
            return
        }

        binding.registerButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val username = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()

            when {
                !isValidEmail(email) -> showSnackbar(getString(R.string.invalid_email_message))
                !isValidUsername(username) -> showSnackbar(getString(R.string.invalid_username_message))
                !isValidPassword(password) -> showSnackbar(getString(R.string.invalid_password_message))
                else -> registerUser(email, username, password)
            }
        }

        binding.loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser(email: String, username: String, password: String) {
        authRepository.register(email, username, password,
            onSuccess = { user ->
                sharedPrefManager.saveUser(user)
                sharedPrefManager.setIsOnboarded(false)
                startActivity(Intent(this, StudentOnboarding::class.java))
                finish()
            },
            onFailure = {
                showSnackbar(getString(R.string.registration_failed_message))
                sharedPrefManager.clearUser()
                sharedPrefManager.clearPreferences()
            })
    }

    private fun isValidEmail(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isValidUsername(username: String) = username.length >= 5

    private fun isValidPassword(password: String) = password.length >= 6

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
