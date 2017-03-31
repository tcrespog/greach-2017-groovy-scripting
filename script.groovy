#!/usr/bin/groovy

@Grab(group='org.jsoup', module='jsoup', version='1.10.2')

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Crawler {

    final static String BASE_URL = 'https://archive.org'
    int resultsLimit
    Document resultsListPage
    List<String> resultsTorrentFilesUrls

    List<String> searchAndDownloadTorrentFiles(String text) {
        downloadResultsPage(text)
        gatherResultsTorrentFilesUrls()
        downloadTorrentFiles()
    }

    void downloadResultsPage(String textToSearch) {
        String searchUrl = buildSearchUrl(textToSearch)

        resultsListPage = resultsListPage = Jsoup.connect(searchUrl).get()
    }

    List<String> gatherResultDetailUrls() {
        List<Element> results = resultsListPage.select('.results .C234 a')

        results.collect { Element resultItem ->
            "${BASE_URL}${resultItem.attr('href')}"
        }.take(resultsLimit)
    }

    void gatherResultsTorrentFilesUrls() {
        List<String> resultsDetailsUrls = gatherResultDetailUrls()

        resultsTorrentFilesUrls = resultsDetailsUrls.findResults { String resultDetailUrl ->
            findTorrentFileDownloadUrl(resultDetailUrl)
        }
    }

    String findTorrentFileDownloadUrl(String documentDetailUrl) {
        println "Connecting to ${documentDetailUrl}"
        Document detailPage = Jsoup.connect(documentDetailUrl).get()

        List<Element> downloadLinks = detailPage.select('.boxy.quick-down a.download-pill')

        Element downloadTorrentLink = downloadLinks.find { Element downloadLink ->
            downloadLink.text().toLowerCase().contains('torrent')
        }

        downloadTorrentLink ? "${BASE_URL}${downloadTorrentLink.attr('href')}" : null
    }

    static String buildSearchUrl(String text) {
        Map params = [sort: '-date']

        params.query = URLEncoder.encode(text, 'UTF-8')

        String queryString = params.collect { String key, String value ->
            "${key}=${value}"
        }.join('&')

        "${BASE_URL}/search.php?${queryString}"
    }

    void downloadTorrentFiles() {
        List<String> command = ['wget', '-nc', '-nv'] + resultsTorrentFilesUrls
        Process process = command.execute()

        StringBuffer error = new StringBuffer()
        process.waitForProcessOutput(null, error)

        println error.toString()
    }

}

void zipFiles() {
    Process listFilesProcess = 'ls'.execute()
    Process filterTorrentsProcess = 'grep .torrent'.execute()

    listFilesProcess | filterTorrentsProcess

    List<String> torrentFiles = filterTorrentsProcess.text.split('\n')

    Process zipProcess = (['zip', 'bundle.zip'] + torrentFiles).execute()
    println zipProcess.text
}

CliBuilder cli = new CliBuilder()
cli.l(args: 1, longOpt:'resultsLimit', argName: 'limit', 'result to crawl')
OptionAccessor options = cli.parse(args)

int limit = options.l ? options.l.toInteger() : 20
Crawler crawler = new Crawler(resultsLimit: limit)
crawler.searchAndDownloadTorrentFiles('mickey mouse')
zipFiles()
