package asynctaskwskt.scl.ifsp.edu.br.asynctaskwskt

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import asynctaskwskt.scl.ifsp.edu.br.asynctaskwskt.MainActivity.constantes.URL_BASE
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL
import android.widget.ProgressBar
import org.json.JSONObject
import org.json.JSONException
import java.io.*
import asynctaskwskt.scl.ifsp.edu.br.asynctaskwskt.R.id.progressBar
import android.widget.TextView




class MainActivity : AppCompatActivity() {

    private var buscarInformacoesBT: Button? = null
    private lateinit var progressBar: ProgressBar

    object constantes {
        val URL_BASE = "http://www.nobile.pro.br/sdm/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Seta o Listener para o botão usando uma função lambda
        buscarInformacoesBt.setOnClickListener {
            // Disparando AsyncTask para buscar texto
            val buscarTextoAt = BuscarTextoAt()
            buscarTextoAt.execute(URL_BASE + "texto.php")
            buscarData(URL_BASE + "data.php");
        }

        progressBar = findViewById(R.id.pb_carregando)
    }

    // AsyncTask que fará acesso ao WebService
    private inner class BuscarTextoAt : AsyncTask<String, Int, String>() {

        // Executa na mesma Thread de UI
        override fun onPreExecute() {
            super.onPreExecute()
            //toast("Buscando String no Web Service")
            // Mostrando a barra de progresso
            progressBar?.visibility = VISIBLE
        }

        // Executa numa outra Thread em background
        override fun doInBackground(vararg params: String?): String {
            // Pegando URL na primeira posição do params
            val url = params[0]
            // Criando um StringBuffer para receber a resposta do Web Service
            val stringBufferResposta: StringBuffer = StringBuffer()
            try {
                // Criando uma conexão HTTP a partir da URL
                val conexao = URL(url).openConnection() as HttpURLConnection
                if (conexao.responseCode == HttpURLConnection.HTTP_OK) {
                    // Caso a conexão seja bem sucedida, resgata o InputStream da mesma
                    val inputStream = conexao.inputStream
                    // Cria um BufferedReader a partir do InputStream
                    val bufferedReader = BufferedInputStream(inputStream).bufferedReader()
                    // Lê o bufferedReader para uma lista de Strings
                    val respostaList = bufferedReader.readLines()
                    // "Appenda" cada String da lista ao StringBuffer
                    respostaList.forEach { stringBufferResposta.append(it) }
                }
            } catch (ioe: IOException) {
                //Toast.makeText(, "Erro na conexão!", Toast.LENGTH_LONG);
            }
            // Simulando notificação do progresso para Thread de UI
            for (i in 1..10) {
                /* Envia um inteiro para ser publicado como progresso. Esse valor é recebido pela função
                callback onProgressUpdate*/
                publishProgress(i)
                // Dormindo por 0.5 s para simular o atraso de rede
                sleep(500)
            }
            // Retorna a String formada a partir do StringBuffer
            return stringBufferResposta.toString()
        }


        // Executa na mesma Thread de UI
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            //toast("Texto recuperado com sucesso")
            // Altera o TextView com o texto recuperado
            textoTv.text = result
            // Tornando a barra de progresso invisível
            progressBar.visibility = View.GONE
        }


        // Executa na Thread de UI, é chamado sempre após uma publishProgress
        override fun onProgressUpdate(vararg values: Int?) {
            // Se o valor de progresso não for nulo, atualiza a barra de progresso
            values[0]?.apply { progressBar.progress = this }
        }
    }

    fun  buscarData(url : String) {

        val buscaDataTask = BuscaDataTask()

    }


    inner class BuscaDataTask : AsyncTask<String, Void, JSONObject>() {

        override fun onPreExecute() {
            super.onPreExecute();
            progressBar.visibility = VISIBLE;
        }

        override fun doInBackground(vararg params: String) = try {
            val url = params[0]
            val connection = URL(url).openConnection() as HttpURLConnection
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            }else{
                null
            }
        } catch (ioe: IOException) {
            ioe.printStackTrace()
            null
        } catch (jsone: JSONException) {
            jsone.printStackTrace()
            null
        }

        override fun onPostExecute(s: JSONObject?) {

            val dataText = s?.let {
                """
                    ${s.getInt("mday")}/${s.getInt("mon")}/${s.getInt("year")}
                    ${s.getInt("hours")}:${s.getInt("minutes")}:${s.getInt("seconds")}
                    ${s.getInt("weekday")}
                """
            } ?: "Falha na obtenção da data"
            val tvData = findViewById<TextView>(R.id.tv_data)
            tvData.text = dataText
            progressBar.visibility = View.GONE
        }
    }

}