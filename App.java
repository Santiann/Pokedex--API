package Pokemon;

import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class App {
	static int pokeNum=0;
	    public static void main(String[] args) throws JSONException {
	    	
	    	JSONObject object = new HTTPRequest().requestGetMethod("https://pokeapi.co/api/v2/pokemon?limit=151");

	    	
	    	JSONArray array = object.getJSONArray("results");

	        for (int index = 0; index < array.length(); index++) {
	            ;

	            JSONObject pokeObject = array.getJSONObject(index);

	            String pokeNome = pokeObject.getString("name");
	            
	            System.out.println("Posicao: " +index + " - " +pokeNome); 
	        }
	        
	       
	        pokeExibe(array);
	        
	        
	        
	    }
	public static void pokeExibe (JSONArray pokeList) {
		 System.out.println("Escolha seu pokemon ");
	        Scanner ler = new Scanner(System.in);
	        try {
				pokeNum = ler.nextInt();
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
	        for(int i = 0; i< pokeList.length(); i++) {
	        	pokeDetails (pokeNum, pokeList ); 
	        }
	    
	}
	public static void pokeDetails (int posicao, JSONArray pokeList) {
		
		try {
			
			JSONObject pokeObject = pokeList.getJSONObject(posicao);
			String pokeNome = pokeObject.getString("name");
			String pokeUrl = pokeObject.getString("url");

			JSONObject poke0 = new HTTPRequest().requestGetMethod(pokeUrl);
			
			JSONArray pokeListaMoves = poke0.getJSONArray("moves");
			JSONArray pokeListaStats = poke0.getJSONArray("stats");
			JSONArray pokeListaTypes = poke0.getJSONArray("types");
			JSONArray pokeListaAbilities = poke0.getJSONArray("abilities");
			JSONObject pokeListaImagem = poke0.getJSONObject("sprites");
			
			int opcao = 0;
			Boolean executou = false;
			
			while (opcao != 2) {
				System.out.println("Selecione 1 para ver as informacoes \n");
				System.out.println("Selecione 2 para sair \n");
				Scanner ler = new Scanner(System.in);
				opcao = ler.nextInt();
				if (opcao == 2) {
					System.exit(1); 
				}
				
				if (!executou) {
					exibeOpcoesPoke(opcao , pokeListaMoves , pokeListaStats , pokeListaTypes, pokeListaAbilities, pokeListaImagem);
					executou = true;
				}
				executou = false;
				continue; 
			}
			
			
				
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void exibeOpcoesPoke (int opcao , JSONArray pokeListaMoves , JSONArray pokeListaStats , JSONArray pokeListaTypes, JSONArray pokeListaAbilities, JSONObject pokeListaImagem) {
	
		Exibicao pokeExibicao = new Exibicao();
		
		
		switch (opcao) {
			case 1: {
				System.out.println("_______Movimentos_______");
				pokeExibicao.exibePokeMoves(pokeListaMoves);
				
				System.out.println("_______Status_______");
				pokeExibicao.exibePokeStats(pokeListaStats);
				
				System.out.println("_______Tipos_______");
				pokeExibicao.exibePokeTypes(pokeListaTypes);
				
				System.out.println("_______Habilidades_______");
				pokeExibicao.exibePokeAbilities(pokeListaAbilities);
				
				System.out.println("_______Imagem_______");
				pokeExibicao.exibePokeImagem(pokeNum);
				break;
			}
			
			default:
				System.out.println("Opcao inexistente");
		}
	}
}	
