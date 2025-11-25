package br.com.ceidigital.util;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ImagemZipUtil {
    private static final String IMAGES_DIR = "imagens_produto";
    private static final String ZIP_NAME = "imagens_produto.zip";

    public static String salvarImagemZip(byte[] imagemBytes, String nomeArquivo) throws IOException {
        Path dir = Paths.get(IMAGES_DIR);
        System.out.println("[DEBUG] Caminho do diretório de imagens: " + dir.toAbsolutePath());
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            System.out.println("[DEBUG] Diretório criado: " + dir.toAbsolutePath());
        }
        Path zipPath = dir.resolve(ZIP_NAME);
        System.out.println("[DEBUG] Caminho do arquivo zip: " + zipPath.toAbsolutePath());
        // Cria ou adiciona ao zip
        try (FileOutputStream fos = new FileOutputStream(zipPath.toFile(), true);
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {
            ZipEntry entry = new ZipEntry(nomeArquivo);
            zos.putNextEntry(entry);
            zos.write(imagemBytes);
            zos.closeEntry();
            System.out.println("[DEBUG] Imagem adicionada ao zip: " + nomeArquivo);
        } catch (Exception e) {
            System.out.println("[ERROR] Falha ao salvar imagem no zip: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return IMAGES_DIR + "/" + ZIP_NAME + "!" + nomeArquivo;
    }

    // Extrai imagem do zip dado o caminho salvo no banco (formato: imagens_produto/imagens_produto.zip!nomeArquivo)
    public static byte[] extrairImagem(String caminho) throws IOException {
        if (caminho == null || !caminho.contains("!")) throw new FileNotFoundException("Caminho inválido");
        String[] partes = caminho.split("!");
        Path zipPath = Paths.get(partes[0]);
        String nomeArquivo = partes[1];
        try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(zipPath.toFile())) {
            ZipEntry entry = zipFile.getEntry(nomeArquivo);
            if (entry == null) throw new FileNotFoundException("Imagem não encontrada no zip");
            try (InputStream is = zipFile.getInputStream(entry)) {
                return is.readAllBytes();
            }
        }
    }
}
