package br.com.uteeth3pi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import br.com.uteeth3pi.databinding.FragmentCriarContaBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class CriarContaFragment : Fragment() {

    private var _binding: FragmentCriarContaBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var functions: FirebaseFunctions
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val gson = GsonBuilder().enableComplexMapKeySerialization().create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

      _binding = FragmentCriarContaBinding.inflate(inflater, container, false)
      return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.imgbArrow.setOnClickListener {
            findNavController().navigate(R.id.action_CriarContaFragment_to_LoginFragment)

        }

        binding.btnNext.setOnClickListener { view ->
            criarNovaConta(
                binding.etEmailCreate.text.toString(),
                binding.etEmailCreate.text.toString(),
                binding.etNameCreate.text.toString(),
                binding.etTelefoneCreate.text.toString(),
                binding.etAdress1.text.toString(),
                binding.etAdress2.text.toString(),
                binding.etAdress3.text.toString()
            )

            findNavController().navigate(R.id.action_CriarContaFragment_to_LoginFragment)
        }
        fun JSONObject.toMap(): Map<String, *> = keys().asSequence().associateWith {
            when (val value = this[it])
            {
                is JSONArray ->
                {
                    val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
                    JSONObject(map).toMap().values.toList()
                }
                is JSONObject -> value.toMap()
                JSONObject.NULL -> null
                else            -> value
            }
        }
    }

    private fun criarNovaConta (nome: String, email: String, senha: String, telefone: String, endereco1: String, endereco2: String, endereco3: String): Task<CustomResponse> {
        auth = Firebase.auth


        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(requireActivity()) { cadastro ->
                if (cadastro.isSuccessful) {
                    val snackbar =
                        Snackbar.make(requireView(), "Você foi cadastrado!", Snackbar.LENGTH_SHORT)
                    snackbar.setBackgroundTint(Color.GREEN)
                    snackbar.show()
                    binding.etEmailCreate.setText("")
                    binding.etPasswordCreate.setText("")

                }
            }.addOnFailureListener { exception ->
                val mensagemErro = when (exception) {
                    is FirebaseAuthWeakPasswordException -> "Digite uma senha com no mínimo 6 caracteres!"
                    is FirebaseAuthInvalidCredentialsException -> "Digite um email válido!"
                    is FirebaseAuthUserCollisionException -> "Esta conta já foi cadastrada!"
                    is FirebaseNetworkException -> "Sem conexão com a internet!"
                    else -> "Erro ao cadastrar usuário!"
                }
                val snackbar = Snackbar.make(requireView(), mensagemErro, Snackbar.LENGTH_SHORT)
                snackbar.setBackgroundTint(Color.RED)
                snackbar.show()

            }

        functions = Firebase.functions("southamerica-east1")

        val data = hashMapOf(
            "name" to nome,
            "email" to email,
            "phone" to telefone,
            "fcmToken" to "456",
            "address[address1]" to endereco1,
            "address[address2]" to endereco2,
            "address[address3]" to endereco3
        )

        return functions
            .getHttpsCallable("createDentista")
            .call(data)
            .continueWith { task ->

                val result = gson.fromJson((task.result?.data as String), CustomResponse::class.java)
                result
            }






    }



    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }
override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}